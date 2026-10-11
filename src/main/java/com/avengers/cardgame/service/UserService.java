package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

/**
 * Сервис для работы с пользователями.
 */
public class UserService {

    private final UserRepository userRepository;


    /**
     * @param userRepository репозиторий пользователей
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Возвращает пользователя по идентификатору, регистрируя нового,
     * если его ещё нет.
     *
     * @param userId      идентификатор пользователя
     * @param defaultName имя по умолчанию для нового пользователя
     * @return существующий или только что созданный пользователь
     */
    public User registerIfAbsent(long userId, String defaultName) {
        return userRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User newUser = new User(userId, defaultName);
                    userRepository.save(newUser);
                    return newUser;
                });
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    public User getByUserId(long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Обновляет отображаемое имя пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param newName новое отображаемое имя
     * @throws UserNotFoundException если пользователь не найден
     */
    public void updateDisplayName(long userId, String newName) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withDisplayName(newName));
    }

    /**
     * Обновляет описание профиля пользователя.
     *
     * @param userId идентификатор пользователя
     * @param newBio новое описание профиля
     * @throws UserNotFoundException если пользователь не найден
     */
    public void updateBio(long userId, String newBio) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withBio(newBio));
    }
}