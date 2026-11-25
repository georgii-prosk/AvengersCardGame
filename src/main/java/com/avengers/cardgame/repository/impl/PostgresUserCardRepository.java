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
import java.sql.SQLException;
import java.util.List;
import java.util.OptionalInt;

/**
 * Реализация {@link UserCardRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresUserCardRepository extends JdbcSupport implements UserCardRepository {

    /** DAO карточек пользователей. */
    private final UserCardDao userCardDao = new UserCardDao();

    /** DAO пользователей. */
    private final UserDao userDao = new UserDao();

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresUserCardRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void addCard(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection()) {
            userCardDao.addCard(connection, userId, heroId);
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
            if (userCardDao.removeCard(connection, userId, heroId, count) == 0) {
                throw new NotEnoughCardsException(heroId, count,
                        userCardDao.countCards(connection, userId, heroId));
            }
            OptionalInt balance = userDao.addShards(connection, userId, shardsGain);
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
            OptionalInt balance = userDao.spendShards(connection, userId, shardsCost);
            if (balance.isEmpty()) {
                OptionalInt available = userDao.findShards(connection, userId);
                if (available.isEmpty()) {
                    throw new UserNotFoundException(userId);
                }
                throw new InsufficientShardsException(shardsCost, available.getAsInt());
            }
            userCardDao.addCard(connection, userId, heroId);
            return balance.getAsInt();
        });
    }

    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return userCardDao.findByUserId(connection, userId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding cards by user", exception);
        }
    }

    @Override
    public int countByUserIdAndHeroId(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return userCardDao.countCards(connection, userId, heroId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error counting cards by user and hero", exception);
        }
    }
}
