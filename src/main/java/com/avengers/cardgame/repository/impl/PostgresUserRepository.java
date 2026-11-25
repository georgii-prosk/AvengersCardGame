package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 */
public class PostgresUserRepository implements UserRepository {

    /** SQL-код нарушения уникальности в PostgreSQL. */
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /** DAO пользователей. */
    private final UserDao userDao = new UserDao();

    /** DAO карточек пользователей. */
    private final UserCardDao userCardDao = new UserCardDao();

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public @NotNull Optional<User> findByUserId(long userId) {
        try (Connection connection = connectionFactory.getConnection()) {
            Optional<User> user = userDao.findById(connection, userId);
            if (user.isEmpty()) {
                return Optional.empty();
            }
            int cardCount = userCardDao.countByUserId(connection, userId);
            return Optional.of(user.get().withCardCount(cardCount));
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding user", exception);
        }
    }

    @Override
    public void save(@NotNull User user) throws UserAlreadyExistsException {
        try (Connection connection = connectionFactory.getConnection()) {
            userDao.insert(connection, user);
        } catch (SQLException exception) {
            if (UNIQUE_VIOLATION_SQL_STATE.equals(exception.getSQLState())) {
                throw new UserAlreadyExistsException(user.userId());
            }
            throw new RuntimeException("Error saving user", exception);
        }
    }

    @Override
    public void update(@NotNull User user) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection()) {
            if (!userDao.update(connection, user)) {
                throw new UserNotFoundException(user.userId());
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating user", exception);
        }
    }

    @Override
    public void updateLastDailyAt(long userId, @NotNull LocalDateTime lastDailyAt) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection()) {
            if (!userDao.updateLastDailyAt(connection, userId, lastDailyAt)) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating last daily time", exception);
        }
    }

    @Override
    public @NotNull List<User> findUsersToNotify(@NotNull LocalDateTime thresholdTime) {
        try (Connection connection = connectionFactory.getConnection()) {
            List<User> users = userDao.findUsersToNotify(connection, thresholdTime);
            List<User> result = new ArrayList<>(users.size());
            for (User user : users) {
                result.add(user.withCardCount(userCardDao.countByUserId(connection, user.userId())));
            }
            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding users to notify", exception);
        }
    }

    @Override
    public void updateDailyNotifiedAt(long userId, @NotNull LocalDateTime notifiedAt) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection()) {
            if (!userDao.updateDailyNotifiedAt(connection, userId, notifiedAt)) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating daily notified time", exception);
        }
    }
}
