package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link RegisteringCommandHandler}.
 */
class RegisteringCommandHandlerTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private InMemoryUserRepository userRepository;
    private UserService userService;
    private FakeBot bot;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
        bot = new FakeBot();
    }

    @Test
    @DisplayName("Регистрирует нового пользователя перед вызовом делегата")
    void registersNewUserBeforeDelegate() throws Exception {
        CommandHandler delegate = _ -> { /* ничего не отправляет */ };
        CommandHandler registering = new RegisteringCommandHandler(delegate, userService);

        registering.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Использует Avenger при отсутствии username")
    void usesDefaultNameWhenUsernameMissing() throws Exception {
        CommandHandler delegate = _ -> { };
        CommandHandler registering = new RegisteringCommandHandler(delegate, userService);

        registering.handle(new CommandContext(USER_ID, CHAT_ID, null, "", bot));

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Использует Avenger при пустом username")
    void usesDefaultNameWhenUsernameBlank() throws Exception {
        CommandHandler delegate = _ -> { };
        CommandHandler registering = new RegisteringCommandHandler(delegate, userService);

        registering.handle(new CommandContext(USER_ID, CHAT_ID, "   ", "", bot));

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Не перезаписывает существующего пользователя")
    void doesNotOverwriteExistingUser() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Original")));

        CommandHandler delegate = _ -> { };
        CommandHandler registering = new RegisteringCommandHandler(delegate, userService);

        registering.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals("Original",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Вызывает делегата с исходным контекстом")
    void callsDelegateWithOriginalContext() throws Exception {
        long[] capturedUserId = new long[1];
        String[] capturedUserName = new String[1];
        String[] capturedArgument = new String[1];
        long[] capturedChatId = new long[1];

        CommandHandler delegate = context -> {
            capturedUserId[0] = context.userId();
            capturedChatId[0] = context.chatId();
            capturedUserName[0] = context.userName();
            capturedArgument[0] = context.argument();
        };
        CommandHandler registering = new RegisteringCommandHandler(delegate, userService);

        registering.handle(new CommandContext(42L, CHAT_ID, "Tony", "Narek", bot));

        Assertions.assertEquals(42L, capturedUserId[0]);
        Assertions.assertEquals(CHAT_ID, capturedChatId[0]);
        Assertions.assertEquals("Tony", capturedUserName[0]);
        Assertions.assertEquals("Narek", capturedArgument[0]);
    }
}