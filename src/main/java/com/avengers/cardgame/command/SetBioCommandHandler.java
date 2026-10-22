package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.InvalidBioException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /setbio.
 */
public class SetBioCommandHandler implements CommandHandler {

    /** Текст ошибки при невалидном bio. */
    private static final String ERROR = "Вы ввели некорректное описание профиля. "
            + "Допустимо использовать до 256 символов латиницей или кириллицей "
            + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
            + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы обновили описание своего профиля на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public SetBioCommandHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        Bio newBio;
        try {
            newBio = Bio.of(context.argument());
        } catch (InvalidBioException e) {
            context.bot().sendMessage(new Message(ERROR), context.chatId());
            return;
        }

        userService.updateBio(context.userId(), newBio);
        context.bot().sendMessage(new Message(SUCCESS_PREFIX + newBio.value()), context.chatId());
    }
}