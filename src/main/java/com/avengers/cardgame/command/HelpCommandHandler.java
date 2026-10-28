package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.Message;

/**
 * Обработчик команды /help.
 */
public class HelpCommandHandler implements CommandHandler {

    /** Текст справки. */
    private static final String HELP_TEXT = """
            /start - запуск бота.
            /help - помощь.
            /profile - информация о твоём профиле в боте.
            /daily - получить ежедневные карточки.
            /inventory - показать твою коллекцию карточек.
            /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
            /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
            """;

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(new Message(HELP_TEXT), context.chatId());
    }
}