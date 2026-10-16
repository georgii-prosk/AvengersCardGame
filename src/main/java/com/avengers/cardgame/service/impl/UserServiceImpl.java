package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;

import java.util.Optional;

/**
 * Реализация {@link UserService} на основе {@link UserRepository}.
 */
public class UserServiceImpl implements UserService {

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /**
     * @param userRepository репозиторий пользователей
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Возвращает пользователя или регистрирует нового.
     *
     * @param userId      идентификатор пользователя
     * @param defaultName имя по умолчанию
     * @return пользователь
     * @throws UserAlreadyExistsException если пользователь уже создан
     */
    @Override
    public User registerIfAbsent(long userId, PlayerName defaultName)
            throws UserAlreadyExistsException {
        Optional<User> existing = userRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        User newUser = new User(userId, defaultName);
        userRepository.save(newUser);
        return newUser;
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public User getByUserId(long userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * Обновляет отображаемое имя существующего пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param newName новое имя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public void updateDisplayName(long userId, PlayerName newName)
            throws UserNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withPlayerName(newName));
    }

    /**
     * Обновляет описание профиля существующего пользователя.
     *
     * @param userId идентификатор пользователя
     * @param newBio новое описание
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public void updateBio(long userId, Bio newBio) throws UserNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withBio(newBio));
    }
}