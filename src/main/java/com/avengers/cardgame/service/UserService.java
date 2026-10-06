package com.avengers.cardgame.service;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

/**
 * Сервис для работы с пользователями.
 * <p>
 * Содержит бизнес-логику регистрации и обновления данных пользователей,
 * делегируя доступ к хранилищу в {@link UserRepository}.
 */
public class UserService {

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /**
     * Создаёт сервис пользователей.
     *
     * @param userRepository репозиторий для доступа к данным пользователей
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Возвращает пользователя по его Telegram-идентификатору, регистрируя
     * нового пользователя с указанным именем, если он ещё не существует.
     *
     * @param telegramId  идентификатор пользователя в Telegram
     * @param defaultName имя по умолчанию для нового пользователя
     * @return существующий или только что созданный пользователь
     */
    public User registerIfAbsent(long telegramId, String defaultName) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    User newUser = new User(telegramId, defaultName);
                    userRepository.save(newUser);
                    return newUser;
                });
    }

    /**
     * Возвращает пользователя по его Telegram-идентификатору.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return найденный пользователь
     * @throws RuntimeException если пользователь не найден
     */
    public User getByTelegramId(long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
    }

    /**
     * Обновляет отображаемое имя пользователя.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param newName    новое отображаемое имя
     * @throws RuntimeException если пользователь не найден
     */
    public void updateDisplayName(long telegramId, String newName) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
        user.setDisplayName(newName);
        userRepository.update(user);
    }

    /**
     * Обновляет описание профиля пользователя.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @param newBio     новое описание профиля
     * @throws RuntimeException если пользователь не найден
     */
    public void updateBio(long telegramId, String newBio) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
        user.setBio(newBio);
        userRepository.update(user);
    }
}