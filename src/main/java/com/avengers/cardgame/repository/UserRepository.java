package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
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
     * @throws UserAlreadyExistsException если пользователь с таким идентификатором уже существует
     */
    void save(User user) throws UserAlreadyExistsException;

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user пользователь с обновлёнными данными
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void update(User user) throws UserNotFoundException;
}