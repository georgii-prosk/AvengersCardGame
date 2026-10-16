package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.command.CommandContext;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.ResolvedCommand;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.MessageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Реализация {@link MessageProcessingService}, работающая с командами бота.
 * <p>
 * Находит команду по тексту, формирует {@link CommandContext}
 * и вызывает обработчик. Ответ на неизвестную команду формирует сам.
 */
public class CommandMessageProcessingService implements MessageProcessingService {

    /** Текст ответа на неизвестную команду. */
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

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

    /**
     * Обрабатывает сообщение: находит команду и вызывает её,
     * либо отправляет сообщение о неизвестной команде.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя, может быть {@code null}
     * @param chatId   идентификатор чата
     * @param text     полный текст сообщения
     * @param bot      бот для отправки ответов
     */
    @Override
    public void processMessage(long userId, String userName, long chatId, String text, Bot bot) {
        Optional<ResolvedCommand> resolved = registry.resolve(text);
        if (resolved.isEmpty()) {
            log.debug("Unknown command: {}", text);
            bot.sendMessage(new Message(UNKNOWN_COMMAND_MESSAGE), chatId);
            return;
        }

        ResolvedCommand rc = resolved.get();
        CommandContext context = new CommandContext(
                userId, chatId, userName, rc.argument(), bot
        );
        rc.command().handle(context);
    }
}