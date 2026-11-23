package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Реализация {@link UserCardRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresUserCardRepository extends JdbcSupport implements UserCardRepository {

    /** Запрос для поиска карточек пользователя. */
    private static final String SQL_FIND_BY_USER =
            "SELECT user_id, hero_id, count FROM user_cards WHERE user_id = ?";

    /** Запрос для подсчёта количества копий конкретной карточки. */
    private static final String SQL_COUNT_BY_USER_AND_HERO =
            "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ? AND hero_id = ?";

    /** SQL-операции экономики. */
    private final EconomySql economySql = new EconomySql();

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresUserCardRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void addCard(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection()) {
            economySql.addCard(connection, userId, heroId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding card", exception);
        }
    }

    @Override
    public int disassemble(long userId, long heroId, int count, int shardsGain) throws AppException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to remove must be positive");
        }
        return inTransaction(connection -> {
            if (!economySql.removeCard(connection, userId, heroId, count)) {
                throw new NotEnoughCardsException(heroId, count,
                        economySql.countCards(connection, userId, heroId));
            }
            OptionalInt balance = economySql.addShards(connection, userId, shardsGain);
            if (balance.isEmpty()) {
                throw new UserNotFoundException(userId);
            }
            return balance.getAsInt();
        });
    }

    @Override
    public int craft(long userId, long heroId, int shardsCost) throws AppException {
        if (shardsCost <= 0) {
            throw new IllegalArgumentException("Craft cost must be positive");
        }
        return inTransaction(connection -> {
            OptionalInt balance = economySql.spendShards(connection, userId, shardsCost);
            if (balance.isEmpty()) {
                OptionalInt available = economySql.findShards(connection, userId);
                if (available.isEmpty()) {
                    throw new UserNotFoundException(userId);
                }
                throw new InsufficientShardsException(shardsCost, available.getAsInt());
            }
            economySql.addCard(connection, userId, heroId);
            return balance.getAsInt();
        });
    }

    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        List<UserCard> cards = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cards.add(new UserCard(
                            resultSet.getLong("user_id"),
                            resultSet.getLong("hero_id"),
                            resultSet.getInt("count")
                    ));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding cards by user", exception);
        }
        return cards;
    }

    @Override
    public int countByUserIdAndHeroId(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT_BY_USER_AND_HERO)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error counting cards by user and hero", exception);
        }
        return 0;
    }
}