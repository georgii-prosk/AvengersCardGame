package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link SetNameCommandHandler}.
 */
class SetNameCommandHandlerTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private InMemoryUserRepository userRepository;
    private FakeBot bot;
    private SetNameCommandHandler command;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        bot = new FakeBot();
        command = new SetNameCommandHandler(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Обновляет имя при валидном вводе")
    void updatesNameWhenValid() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("OldName")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "Narek", bot));

        Assertions.assertEquals("Вы сменили отображаемое имя на Narek", bot.lastSent().message().text());
        Assertions.assertEquals("Narek",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Отправляет ошибку при невалидном имени")
    void sendsErrorWhenInvalid() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("OldName")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "bad name", bot));

        Assertions.assertEquals(
                "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.",
                bot.lastSent().message().text());
        Assertions.assertEquals("OldName",
                userRepository.findByUserId(USER_ID).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Отправляет ошибку при пустом аргументе")
    void sendsErrorWhenEmptyArgument() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("OldName")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals(
                "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет, что команда бросает UserNotFoundException, если пользователя нет.
     */
    @Test
    @DisplayName("Бросает UserNotFoundException, если пользователя нет")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "Narek", bot)));
    }
}