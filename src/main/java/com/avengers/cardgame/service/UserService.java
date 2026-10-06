package com.avengers.cardgame.service;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerIfAbsent(long telegramId, String defaultName) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    User newUser = new User(telegramId, defaultName);
                    userRepository.save(newUser);
                    return newUser;
                });
    }

    public User getByTelegramId(long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
    }

    public void updateDisplayName(long telegramId, String newName) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
        user.setDisplayName(newName);
        userRepository.update(user);
    }

    public void updateBio(long telegramId, String newBio) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found: " + telegramId));
        user.setBio(newBio);
        userRepository.update(user);
    }
}