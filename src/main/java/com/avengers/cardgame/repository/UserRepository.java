package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.User;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
public interface UserRepository {

    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByUserId(long userId);

    /**
     * Сохраняет нового пользователя.
     *
     * @param user пользователь для сохранения
     */
    void save(User user);

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user пользователь с обновлёнными данными
     */
    void update(User user);
}