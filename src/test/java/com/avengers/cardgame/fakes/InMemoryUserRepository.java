package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Фейковый репозиторий пользователей в памяти.
 * <p>
 * Полноценная реализация {@link UserRepository} без обращения к БД.
 * Используется в тестах.
 */
public class InMemoryUserRepository implements UserRepository {

    /** Хранилище пользователей по идентификатору. */
    private final Map<Long, User> storage = new HashMap<>();

    @Override
    public Optional<User> findByUserId(long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void save(User user) throws UserAlreadyExistsException {
        if (storage.containsKey(user.userId())) {
            throw new UserAlreadyExistsException(user.userId());
        }
        storage.put(user.userId(), user);
    }

    @Override
    public void update(User user) throws UserNotFoundException {
        if (!storage.containsKey(user.userId())) {
            throw new UserNotFoundException(user.userId());
        }
        storage.put(user.userId(), user);
    }

    /**
     * Возвращает количество сохранённых пользователей.
     *
     * @return количество пользователей
     */
    public int size() {
        return storage.size();
    }
}