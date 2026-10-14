package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link RegisteringCommand}.
 */
class RegisteringCommandTest {

    /** Фейковый репозиторий пользователей. */
    private FakeUserRepository userRepository;

    /** Тестируемый сервис. */
    private UserServiceImpl userService;

    /**
     * Создаёт фейковый репозиторий и сервис перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @DisplayName("Регистрирует нового пользователя перед вызовом делегата")
    void registersNewUserBeforeDelegate() throws Exception {
        CommandHandler delegate = (userId, userName, argument) -> new Reply("ok");
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        registering.handle(1L, "Tony", "");

        Assertions.assertTrue(userRepository.findByUserId(1L).isPresent());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Использует Avenger при отсутствии username")
    void usesDefaultNameWhenUsernameMissing() throws Exception {
        CommandHandler delegate = (userId, userName, argument) -> new Reply("ok");
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        registering.handle(1L, null, "");

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Использует Avenger при пустом username")
    void usesDefaultNameWhenUsernameBlank() throws Exception {
        CommandHandler delegate = (userId, userName, argument) -> new Reply("ok");
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        registering.handle(1L, "   ", "");

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Не перезаписывает существующего пользователя")
    void doesNotOverwriteExistingUser() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Original")));

        CommandHandler delegate = (userId, userName, argument) -> new Reply("ok");
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        registering.handle(1L, "Tony", "");

        Assertions.assertEquals("Original",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Возвращает ответ делегата")
    void returnsDelegateReply() throws Exception {
        CommandHandler delegate = (userId, userName, argument) -> new Reply("delegate answer");
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        Reply reply = registering.handle(1L, "Tony", "");

        Assertions.assertEquals("delegate answer", reply.text());
    }

    @Test
    @DisplayName("Передаёт параметры делегату без изменений")
    void passesParametersToDelegate() throws Exception {
        long[] capturedUserId = new long[1];
        String[] capturedUserName = new String[1];
        String[] capturedArgument = new String[1];

        CommandHandler delegate = (userId, userName, argument) -> {
            capturedUserId[0] = userId;
            capturedUserName[0] = userName;
            capturedArgument[0] = argument;
            return new Reply("ok");
        };
        CommandHandler registering = new RegisteringCommand(delegate, userService);

        registering.handle(42L, "Tony", "Narek");

        Assertions.assertEquals(42L, capturedUserId[0]);
        Assertions.assertEquals("Tony", capturedUserName[0]);
        Assertions.assertEquals("Narek", capturedArgument[0]);
    }
}