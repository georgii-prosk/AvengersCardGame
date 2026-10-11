package com.avengers.cardgame.command;

/**
 * Запрос на обработку команды.
 *
 * @param userId   идентификатор пользователя
 * @param userName отображаемое имя пользователя, может быть {@code null}
 * @param argument текст после имени команды
 */
public record CommandRequest(long userId, String userName, String argument) {
}