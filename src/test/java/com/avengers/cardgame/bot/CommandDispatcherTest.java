package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.CommandHandler;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.command.ProfileCommand;
import com.avengers.cardgame.command.Reply;
import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link CommandDispatcher}.
 * <p>
 * Проверяют маршрутизацию сообщений, ответ на неизвестную команду
 * и обработку доменных исключений.
 */
class CommandDispatcherTest {

    /** Текст ответа на неизвестную команду. */
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

    /** Текст ответа при ошибке обработки. */
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    /** Фейковый репозиторий пользователей. */
    private FakeUserRepository userRepository;

    /** Тестируемый диспетчер. */
    private MessageDispatcher dispatcher;

    /**
     * Создаёт фейковый репозиторий, сервис и диспетчер перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository);
        CommandRegistry registry = new CommandRegistryFactory(userService).create();
        dispatcher = new CommandDispatcher(registry);
    }

    @Test
    @DisplayName("Маршрутизирует /start и возвращает приветствие")
    void dispatchesStartCommand() {
        Reply reply = dispatcher.dispatch(1L, "Tony", "/start");

        Assertions.assertTrue(reply.text().contains("AvengersCardGame"));
    }

    @Test
    @DisplayName("Маршрутизирует /help и возвращает справку")
    void dispatchesHelpCommand() {
        Reply reply = dispatcher.dispatch(1L, "Tony", "/help");

        Assertions.assertTrue(reply.text().contains("/start"));
        Assertions.assertTrue(reply.text().contains("/help"));
    }

    @Test
    @DisplayName("Регистрирует пользователя перед /profile")
    void registersUserBeforeProfile() {
        dispatcher.dispatch(1L, "Tony", "/profile");

        Assertions.assertTrue(userRepository.findByUserId(1L).isPresent());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Возвращает UNKNOWN_COMMAND_MESSAGE для неизвестной команды")
    void returnsUnknownForUnknownCommand() {
        Reply reply = dispatcher.dispatch(1L, "Tony", "/foobar");

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, reply.text());
    }

    @Test
    @DisplayName("Возвращает UNKNOWN_COMMAND_MESSAGE для пустой строки")
    void returnsUnknownForBlankText() {
        Reply reply = dispatcher.dispatch(1L, "Tony", "   ");

        Assertions.assertEquals(UNKNOWN_COMMAND_MESSAGE, reply.text());
    }

    @Test
    @DisplayName("Возвращает ERROR_MESSAGE при UserNotFoundException")
    void returnsErrorOnUserNotFound() {
        CommandRegistry bareRegistry = new CommandRegistry();
        bareRegistry.register("/profile",
                new ProfileCommand(new UserServiceImpl(userRepository)));
        MessageDispatcher bareDispatcher = new CommandDispatcher(bareRegistry);

        Reply reply = bareDispatcher.dispatch(999L, "Tony", "/profile");

        Assertions.assertEquals(ERROR_MESSAGE, reply.text());
    }

    @Test
    @DisplayName("Возвращает ERROR_MESSAGE при UserAlreadyExistsException")
    void returnsErrorOnUserAlreadyExists() {
        CommandHandler throwing = (userId, ignoredUserName, ignoredArgument) -> {
            throw new UserAlreadyExistsException(userId);
        };
        CommandRegistry registry = new CommandRegistry();
        registry.register("/test", throwing);
        MessageDispatcher localDispatcher = new CommandDispatcher(registry);

        Reply reply = localDispatcher.dispatch(1L, "Tony", "/test");

        Assertions.assertEquals(ERROR_MESSAGE, reply.text());
    }

    @Test
    @DisplayName("Не увеличивает количество пользователей при повторных вызовах /start")
    void doesNotDuplicateUserOnRepeatedStart() {
        dispatcher.dispatch(1L, "Tony", "/start");
        dispatcher.dispatch(1L, "Tony", "/start");
        dispatcher.dispatch(1L, "Tony", "/start");

        Assertions.assertEquals(1, userRepository.size());
    }

    @Test
    @DisplayName("Различает разных пользователей")
    void handlesDifferentUsers() {
        dispatcher.dispatch(1L, "Tony", "/start");
        dispatcher.dispatch(2L, "Steve", "/start");

        Assertions.assertEquals(2, userRepository.size());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
        Assertions.assertEquals("Steve",
                userRepository.findByUserId(2L).orElseThrow().playerName().value());
    }
}