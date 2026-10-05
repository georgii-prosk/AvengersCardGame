
package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByTelegramId(long telegramId);
    void save(User user);
    void update(User user);
}