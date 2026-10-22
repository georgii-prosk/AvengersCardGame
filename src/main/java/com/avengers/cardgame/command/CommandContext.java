package com.avengers.cardgame.command;

import com.avengers.cardgame.bot.Bot;

/**
 * Контекст выполнения команды.
 * <p>
 * Содержит всё, что нужно команде: кто вызвал, куда отвечать,
 * аргумент и бота для отправки ответов.
 *
 * @param userId   идентификатор пользователя
 * @param chatId   идентификатор чата
 * @param userName имя пользователя, может быть {@code null}
 * @param argument аргумент команды
 * @param bot      бот для отправки ответов
 */
public record CommandContext(
        long userId,
        long chatId,
        String userName,
        String argument,
        Bot bot
) {
}