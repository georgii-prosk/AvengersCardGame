package com.avengers.cardgame.service;

import com.avengers.cardgame.bot.Bot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        @Nullable String userName,
        @NotNull String argument,
        @NotNull Bot bot
) {
}