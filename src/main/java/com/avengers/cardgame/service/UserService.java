package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;

import java.time.LocalDateTime;

/**
 * Сервис для работы с пользователями.
 */
public interface UserService {

    /**
     * Возвращает пользователя по идентификатору, регистрируя нового,
     * если его ещё нет.
     *
     * @param userId      идентификатор пользователя
     * @param defaultName имя по умолчанию для нового пользователя
     * @return существующий или только что созданный пользователь
     * @throws UserAlreadyExistsException если параллельно пользователь уже создан
     */
    User registerIfAbsent(long userId, PlayerName defaultName)
            throws UserAlreadyExistsException;

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    User getByUserId(long userId) throws UserNotFoundException;

    /**
     * Обновляет отображаемое имя пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param newName новое отображаемое имя
     * @throws UserNotFoundException если пользователь не найден
     */
    void updateDisplayName(long userId, PlayerName newName)
            throws UserNotFoundException;

    /**
     * Обновляет описание профиля пользователя.
     *
     * @param userId идентификатор пользователя
     * @param newBio новое описание профиля
     * @throws UserNotFoundException если пользователь не найден
     */
    void updateBio(long userId, Bio newBio) throws UserNotFoundException;

    /**
     * Обновляет время последнего получения ежедневной награды.
     *
     * @param userId      идентификатор пользователя
     * @param lastDailyAt новое время получения /daily
     * @throws UserNotFoundException если пользователь не найден
     */
    void updateLastDailyAt(long userId, LocalDateTime lastDailyAt) throws UserNotFoundException;
}