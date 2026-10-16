package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /setname.
 */
public class SetNameCommand extends SafeCommandHandler {

    /** Текст ошибки при невалидном имени. */
    private static final String ERROR = "Вы ввели некорректное имя. "
            + "Допустимо использовать до 128 символов латиницей.";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы сменили отображаемое имя на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public SetNameCommand(UserService userService) {
        this.userService = userService;
    }

    /**
     * Валидирует новое имя и обновляет его в профиле.
     *
     * @param context контекст выполнения команды
     * @throws AppException если пользователь не найден
     */
    @Override
    protected void doHandle(CommandContext context) throws AppException {
        PlayerName newName;
        try {
            newName = new PlayerName(context.argument());
        } catch (IllegalArgumentException e) {
            context.bot().sendMessage(new Message(ERROR), context.chatId());
            return;
        }

        userService.updateDisplayName(context.userId(), newName);
        context.bot().sendMessage(
                new Message(SUCCESS_PREFIX + newName.value()),
                context.chatId()
        );
    }
}