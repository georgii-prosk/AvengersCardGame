package com.avengers.cardgame.service;

import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.exception.InvalidUserException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.service.impl.CommandMessageProcessingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link CommandMessageProcessingService}.
 */
class CommandMessageProcessingServiceTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;
    private static final String USER_NAME = "Tony";

    private static final String FIRST_RESPONSE = "first-response";
    private static final String SECOND_RESPONSE = "second-response";

    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    private FakeBot bot;
    private MessageProcessingService service;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        CommandRegistry registry = new CommandRegistry();
        registry.register("/first", context ->
                context.bot().sendMessage(new Message(FIRST_RESPONSE), context.chatId()));
        registry.register("/second", context ->
                context.bot().sendMessage(new Message(SECOND_RESPONSE), context.chatId()));
        registry.register("/fail", context -> {
            throw new InvalidUserException("boom");
        });
        registry.register("/runtime-fail", context -> {
            throw new RuntimeException("boom");
        });
        service = new CommandMessageProcessingService(registry);
    }

    @Test
    @DisplayName("Маршрутизирует команду в её обработчик")
    void routesFirstCommand() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "/first", bot);

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(FIRST_RESPONSE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Маршрутизирует вторую команду в её обработчик")
    void routesSecondCommand() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "/second", bot);

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(SECOND_RESPONSE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Отправляет сообщение в правильный чат")
    void sendsToCorrectChat() {
        long customChatId = 999L;
        service.processMessage(USER_ID, USER_NAME, customChatId, "/first", bot);

        Assertions.assertEquals(customChatId, bot.lastSent().chatId());
    }

    @Test
    @DisplayName("Отправляет UNKNOWN_COMMAND_MESSAGE для неизвестной команды")
    void sendsUnknownForUnknownCommand() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "/unknown", bot);

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Отправляет UNKNOWN_COMMAND_MESSAGE для пустой строки")
    void sendsUnknownForBlankText() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "   ", bot);

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Отправляет ERROR_MESSAGE при доменной ошибке")
    void sendsErrorOnAppException() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "/fail", bot);

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(ERROR_MESSAGE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Отправляет ERROR_MESSAGE при непредвиденном исключении")
    void sendsErrorOnRuntimeException() {
        service.processMessage(USER_ID, USER_NAME, CHAT_ID, "/runtime-fail", bot);

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(ERROR_MESSAGE, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Не пытается ответить через сломанного бота при BotException")
    void doesNotReplyWhenBotIsBroken() {
        CommandRegistry brokenRegistry = new CommandRegistry();
        brokenRegistry.register("/bot-fail", context -> {
            throw new com.avengers.cardgame.exception.BotException("boom", null);
        });
        CommandMessageProcessingService brokenService = new CommandMessageProcessingService(brokenRegistry);
        FakeBot neverSent = new FakeBot();

        brokenService.processMessage(USER_ID, USER_NAME, CHAT_ID, "/bot-fail", neverSent);

        Assertions.assertEquals(0, neverSent.count());
    }
}