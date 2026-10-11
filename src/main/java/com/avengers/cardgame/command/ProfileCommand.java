package com.avengers.cardgame.command;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /profile.
 */
public class ProfileCommand implements Command {

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


    /**
     * Возвращает данные профиля пользователя.
     *
     * @param request запрос с идентификатором пользователя
     * @return ответ с именем, bio, балансом и количеством карточек
     */
    @Override
    public Reply handle(CommandRequest request) {
        User user = userService.getByUserId(request.userId());

        String displayName = (user.displayName() == null || user.displayName().isBlank())
                ? NOT_SET
                : user.displayName();
        String bio = (user.bio() == null || user.bio().isBlank())
                ? NOT_SET
                : user.bio();

        return new Reply("""
                Имя: %s
                Описание: %s
                Твой баланс: %d щитов
                В твоей коллекции: %d карточек
                """.formatted(displayName, bio, user.balance(), user.cardCount()));
    }
}