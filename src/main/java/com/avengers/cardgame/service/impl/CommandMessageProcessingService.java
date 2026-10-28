package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.command.CommandContext;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.ResolvedCommand;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.MessageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Реализация {@link MessageProcessingService}, работающая с командами бота.
 */
public class CommandMessageProcessingService implements MessageProcessingService {

    /** Текст ответа на неизвестную команду. */
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

    /** Текст ответа при ошибке выполнения команды. */
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(CommandMessageProcessingService.class);

    /** Реестр команд. */
    private final CommandRegistry registry;

    /**
     * @param registry реестр команд
     */
    public CommandMessageProcessingService(CommandRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void processMessage(long userId, String userName, long chatId, String text, Bot bot) {
        Optional<ResolvedCommand> resolved = registry.resolve(text);
        if (resolved.isEmpty()) {
            log.debug("Unknown command: {}", text);
            sendSafely(bot, new Message(UNKNOWN_COMMAND_MESSAGE), chatId);
            return;
        }

        ResolvedCommand rc = resolved.get();
        CommandContext context = new CommandContext(userId, chatId, userName, rc.argument(), bot);

        try {
            rc.command().handle(context);
        } catch (BotException e) {
            // Бот уже сломан — отвечать через него бессмысленно, только логируем.
            log.error("Bot error while executing command", e);
        } catch (AppException e) {
            log.error("Domain error while executing command", e);
            sendSafely(bot, new Message(ERROR_MESSAGE), chatId);
        } catch (Exception e) {
            log.error("Unexpected error while executing command", e);
            sendSafely(bot, new Message(ERROR_MESSAGE), chatId);
        }
    }

    /**
     * Пытается отправить сообщение и логирует ошибку, если это не удалось.
     * Используется в сценариях, где отсутствие ответа не должно прерывать
     * обработку (ответ на неизвестную команду, ответ об ошибке).
     *
     * @param bot     бот
     * @param message сообщение
     * @param chatId  идентификатор чата
     */
    private void sendSafely(Bot bot, Message message, long chatId) {
        try {
            bot.sendMessage(message, chatId);
        } catch (BotException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }
}