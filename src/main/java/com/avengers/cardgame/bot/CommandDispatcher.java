package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.Reply;
import com.avengers.cardgame.command.ResolvedCommand;
import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Реализация {@link MessageDispatcher}, работающая с командами бота.
 * <p>
 * Отвечает за вызов команды, обработку доменных исключений
 * и формирование ответа пользователю.
 */
public class CommandDispatcher implements MessageDispatcher {

    /** Текст ответа на неизвестную команду. */
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

    /** Текст ответа при ошибке обработки. */
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(CommandDispatcher.class);

    /** Реестр команд. */
    private final CommandRegistry commandRegistry;

    /**
     * @param commandRegistry реестр команд
     */
    public CommandDispatcher(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
    }

    /**
     * Обрабатывает текстовое сообщение.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param text     текст сообщения
     * @return ответ пользователю
     */
    @Override
    public Reply dispatch(long userId, String userName, String text) {
        return commandRegistry.resolve(text)
                .map(resolved -> executeCommand(userId, userName, resolved))
                .orElseGet(() -> new Reply(UNKNOWN_COMMAND_MESSAGE));
    }

    /**
     * Выполняет команду и обрабатывает доменные исключения.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param resolved найденная команда с аргументом
     * @return ответ пользователю
     */
    private Reply executeCommand(long userId, String userName, ResolvedCommand resolved) {
        try {
            return resolved.command().handle(userId, userName, resolved.argument());
        } catch (UserNotFoundException | UserAlreadyExistsException e) {
            log.error("Domain error while executing command", e);
            return new Reply(ERROR_MESSAGE);
        }
    }
}