package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Базовый класс обработчика команды с безопасной обработкой доменных ошибок.
 * <p>
 * Реализует {@link #handle} как {@code final}: ловит любые {@link AppException}
 * и отправляет пользователю сообщение об ошибке. Наследники реализуют
 * {@link #doHandle}.
 */
public abstract class SafeCommandHandler implements CommandHandler {

    /** Текст ответа при доменной ошибке. */
    public static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Обрабатывает команду, перехватывая доменные ошибки.
     * <p>
     * При {@link AppException} отправляет пользователю {@link #ERROR_MESSAGE}.
     *
     * @param context контекст выполнения команды
     */
    @Override
    public final void handle(CommandContext context) {
        try {
            doHandle(context);
        } catch (AppException e) {
            log.error("Domain error while executing command", e);
            context.bot().sendMessage(new Message(ERROR_MESSAGE), context.chatId());
        }
    }

    /**
     * Реализация команды.
     *
     * @param context контекст выполнения
     * @throws AppException при доменной ошибке
     */
    protected abstract void doHandle(CommandContext context) throws AppException;
}