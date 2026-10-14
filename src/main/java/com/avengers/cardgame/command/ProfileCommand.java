package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /profile.
 */
public class ProfileCommand implements CommandHandler {

    /** Заглушка для пустого поля профиля. */
    private static final String NOT_SET = "не задано";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public ProfileCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Reply handle(long userId, String userName, String argument)
            throws UserNotFoundException {
        User user = userService.getByUserId(userId);

        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();

        return new Reply("""
                Имя: %s
                Описание: %s
                Твой баланс: %d щитов
                В твоей коллекции: %d карточек
                """.formatted(displayName, bio, user.balance(), user.cardCount()));
    }
}