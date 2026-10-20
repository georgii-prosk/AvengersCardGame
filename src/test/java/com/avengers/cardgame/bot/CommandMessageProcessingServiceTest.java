package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.service.impl.CommandMessageProcessingService;
import com.avengers.cardgame.service.MessageProcessingService;
import com.avengers.cardgame.service.impl.UserServiceImpl;
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

    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

    private InMemoryUserRepository userRepository;
    private FakeBot bot;
    private MessageProcessingService service;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        bot = new FakeBot();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CommandRegistry registry = new CommandRegistryFactory(userService).create();
        service = new CommandMessageProcessingService(registry);
    }

    @Test
    @DisplayName("Маршрутизирует /start и отправляет приветствие")
    void dispatchesStartCommand() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/start", bot);

        Assertions.assertEquals(1, bot.count());
        Assertions.assertTrue(bot.lastText().contains("AvengersCardGame"));
    }

    @Test
    @DisplayName("Маршрутизирует /help и отправляет справку")
    void dispatchesHelpCommand() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/help", bot);

        Assertions.assertTrue(bot.lastText().contains("/start"));
        Assertions.assertTrue(bot.lastText().contains("/help"));
    }

    @Test
    @DisplayName("Регистрирует пользователя перед /profile")
    void registersUserBeforeProfile() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/profile", bot);

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Отправляет UNKNOWN_COMMAND_MESSAGE для неизвестной команды")
    void sendsUnknownForUnknownCommand() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/foobar", bot);

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет UNKNOWN_COMMAND_MESSAGE для пустой строки")
    void sendsUnknownForBlankText() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "   ", bot);

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет сообщение в правильный чат")
    void sendsToCorrectChat() {
        long customChatId = 999L;
        service.processMessage(USER_ID, "Tony", customChatId, "/start", bot);

        Assertions.assertEquals(customChatId, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Не увеличивает количество пользователей при повторных вызовах /start")
    void doesNotDuplicateUserOnRepeatedStart() {
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/start", bot);
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/start", bot);
        service.processMessage(USER_ID, "Tony", CHAT_ID, "/start", bot);

        Assertions.assertEquals(1, userRepository.size());
    }

    @Test
    @DisplayName("Различает разных пользователей")
    void handlesDifferentUsers() {
        service.processMessage(1L, "Tony", CHAT_ID, "/start", bot);
        service.processMessage(2L, "Steve", CHAT_ID, "/start", bot);

        Assertions.assertEquals(2, userRepository.size());
    }
}