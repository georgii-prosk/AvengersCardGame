package com.avengers.cardgame.service;

import com.avengers.cardgame.command.callback.CallbackContext;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.service.impl.CommandCallbackProcessingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link CommandCallbackProcessingService}.
 */
class CommandCallbackProcessingServiceTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeBot bot;
    private CommandCallbackProcessingService service;
    private List<CallbackContext> received;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        service = new CommandCallbackProcessingService();
        received = new ArrayList<>();
    }

    @Test
    @DisplayName("Передаёт нажатие обработчику по префиксу")
    void routesByPrefix() {
        service.register("view_card", received::add);

        service.processCallback(USER_ID, CHAT_ID, "view_card:5", bot);

        Assertions.assertEquals(1, received.size());
        CallbackContext context = received.getFirst();
        Assertions.assertEquals(USER_ID, context.userId());
        Assertions.assertEquals(CHAT_ID, context.chatId());
        Assertions.assertEquals("view_card:5", context.callbackData());
        Assertions.assertEquals("5", context.argument());
    }

    @Test
    @DisplayName("Не вызывает обработчик другого префикса")
    void doesNotCallOtherHandlers() {
        service.register("page", received::add);

        service.processCallback(USER_ID, CHAT_ID, "view_card:5", bot);

        Assertions.assertTrue(received.isEmpty());
        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Игнорирует пустые данные")
    void ignoresBlankData() {
        service.register("page", received::add);

        service.processCallback(USER_ID, CHAT_ID, " ", bot);
        service.processCallback(USER_ID, CHAT_ID, null, bot);

        Assertions.assertTrue(received.isEmpty());
        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("При AppException отправляет сообщение об ошибке")
    void sendsErrorOnAppException() {
        service.register("page", context -> {
            throw new UserNotFoundException(USER_ID);
        });

        service.processCallback(USER_ID, CHAT_ID, "page:1", bot);

        Assertions.assertEquals("Произошла ошибка при обработке нажатия.", bot.lastText());
        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Аргумент пустой, если разделителя нет")
    void argumentIsEmptyWithoutSeparator() {
        CallbackContext context = new CallbackContext(USER_ID, CHAT_ID, "page", bot);

        Assertions.assertEquals("", context.argument());
    }
}
