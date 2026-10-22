package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /profile.
 */
public class ProfileCommandHandler implements CommandHandler {

    /** Шаблон для вывода профиля. */
    private static final String PROFILE_TEMPLATE = """
            Имя: %s
            Описание: %s
            Твой баланс: %d щитов
            В твоей коллекции: %d карточек
            """;

    /** Заглушка для пустого поля профиля. */
    private static final String NOT_SET = "не задано";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public ProfileCommandHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        User user = userService.getByUserId(context.userId());

        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();

        String text = PROFILE_TEMPLATE.formatted(displayName, bio, user.balance(), user.cardCount());

        context.bot().sendMessage(new Message(text), context.chatId());
    }
}