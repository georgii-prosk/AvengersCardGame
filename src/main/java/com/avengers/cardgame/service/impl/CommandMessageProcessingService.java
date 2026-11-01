package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.MessageProcessingService;
import com.avengers.cardgame.service.ResolvedCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public void processMessage(long userId, @Nullable String userName, long chatId, @NotNull String text, @NotNull Bot bot) {
        Optional<ResolvedCommand> resolved = registry.resolve(text);
        if (resolved.isEmpty()) {
            log.debug("Unknown command: {}", text);
            try {
                bot.sendMessage(new Message(UNKNOWN_COMMAND_MESSAGE), chatId);
            } catch (Exception exception) {
                log.error("Failed to send unknown command message to chat {}", chatId, exception);
            }
            return;
        }

        ResolvedCommand resolvedCommand = resolved.get();
        CommandContext context = new CommandContext(userId, chatId, userName, resolvedCommand.argument(), bot);

        try {
            resolvedCommand.command().handle(context);
        } catch (AppException exception) {
            log.error("Domain error while executing command", exception);
            try {
                bot.sendMessage(new Message(ERROR_MESSAGE), chatId);
            } catch (Exception e) {
                log.error("Failed to send error message to chat {}", chatId, e);
            }
        } catch (Exception exception) {
            log.error("Unexpected error while executing command", exception);
            try {
                bot.sendMessage(new Message(ERROR_MESSAGE), chatId);
            } catch (Exception e) {
                log.error("Failed to send error message to chat {}", chatId, e);
            }
        }
    }
}