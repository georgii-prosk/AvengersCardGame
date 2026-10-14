package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;

/**
 * Обработчик команды бота.
 */
public interface CommandHandler {

    /**
     * Обрабатывает команду.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя, может быть {@code null}
     * @param argument текст после имени команды
     * @return ответ пользователю
     * @throws UserNotFoundException      если операция требует существующего пользователя
     * @throws UserAlreadyExistsException если попытка зарегистрировать существующего пользователя
     */
    Reply handle(long userId, String userName, String argument)
            throws UserNotFoundException, UserAlreadyExistsException;
}