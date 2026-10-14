package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.Reply;

/**
 * Обработчик входящих сообщений от пользователя.
 * <p>
 * Не зависит от платформы: принимает обезличенные данные
 * (идентификатор, имя, текст), возвращает ответ.
 */
public interface MessageDispatcher {

    /**
     * Обрабатывает текстовое сообщение и возвращает ответ.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя, может быть {@code null}
     * @param text     полный текст сообщения
     * @return ответ пользователю
     */
    Reply dispatch(long userId, String userName, String text);
}