package com.avengers.cardgame.command;

import com.avengers.cardgame.service.Message;

/**
 * Команда /help.
 */
public class HelpCommand extends SafeCommandHandler {

    /** Текст справки. */
    private static final String HELP_TEXT = """
            /start - запуск бота.
            /help - помощь.
            /profile - информация о твоём профиле в боте.
            /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
            /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
            """;

    /**
     * Отправляет список доступных команд.
     *
     * @param context контекст выполнения команды
     */
    @Override
    protected void doHandle(CommandContext context) {
        context.bot().sendMessage(new Message(HELP_TEXT), context.chatId());
    }
}