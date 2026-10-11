package com.avengers.cardgame.command;

/**
 * Команда /help.
 */
public class HelpCommand implements Command {

    @Override
    public Reply handle(CommandRequest request) {
        return new Reply("""
                /start - запуск бота.
                /help - помощь.
                /profile - информация о твоём профиле в боте.
                /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
                /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
                """);
    }
}