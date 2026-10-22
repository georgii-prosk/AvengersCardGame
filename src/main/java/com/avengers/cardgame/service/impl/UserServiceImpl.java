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

    @Override
    public User getByUserId(long userId) throws UserNotFoundException {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public void updateDisplayName(long userId, PlayerName newName)
            throws UserNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withPlayerName(newName));
    }

    @Override
    public void updateBio(long userId, Bio newBio) throws UserNotFoundException {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        userRepository.update(user.withBio(newBio));
    }
}