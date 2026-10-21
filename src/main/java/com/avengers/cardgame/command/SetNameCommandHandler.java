package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.InvalidPlayerNameException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /setname.
 */
public class SetNameCommandHandler implements CommandHandler {

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
    public SetNameCommandHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        PlayerName newName;
        try {
            newName = PlayerName.of(context.argument());
        } catch (InvalidPlayerNameException e) {
            context.bot().sendMessage(new Message(ERROR), context.chatId());
            return;
        }

        userService.updateDisplayName(context.userId(), newName);
        context.bot().sendMessage(new Message(SUCCESS_PREFIX + newName.value()), context.chatId());
    }
}