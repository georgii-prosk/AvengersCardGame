package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /profile.
 */
public class ProfileCommand extends SafeCommandHandler {

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
     * Отправляет данные профиля пользователя.
     *
     * @param context контекст выполнения команды
     * @throws AppException если пользователь не найден
     */
    @Override
    protected void doHandle(CommandContext context) throws AppException {
        User user = userService.getByUserId(context.userId());

        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();

        String text = """
                Имя: %s
                Описание: %s
                Твой баланс: %d щитов
                В твоей коллекции: %d карточек
                """.formatted(displayName, bio, user.balance(), user.cardCount());

        context.bot().sendMessage(new Message(text), context.chatId());
    }
}