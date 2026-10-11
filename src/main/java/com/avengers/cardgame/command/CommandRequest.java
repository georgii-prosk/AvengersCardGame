package com.avengers.cardgame.command;

/**
 * Запрос на обработку команды.
 *
 * @param userId   идентификатор пользователя
 * @param userName отображаемое имя пользователя, может быть {@code null}
 * @param text     полный текст сообщения, включая имя команды
 */
public record CommandRequest(long userId, String userName, String text) {
}