package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link SetBioCommand}.
 */
class SetBioCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeUserRepository userRepository;
    private FakeBot bot;
    private SetBioCommand command;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        bot = new FakeBot();
        command = new SetBioCommand(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Обновляет bio при валидном вводе")
    void updatesBioWhenValid() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Tony")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "Genius billionaire", bot));

        Assertions.assertEquals(
                "Вы обновили описание своего профиля на Genius billionaire", bot.lastText());
        Assertions.assertEquals("Genius billionaire",
                userRepository.findByUserId(USER_ID).orElseThrow().bio().value());
    }

    @Test
    @DisplayName("Отправляет ошибку при невалидном bio")
    void sendsErrorWhenInvalid() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Tony")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony",
                "Текст с \u0000 нулевым символом", bot));

        String expected = "Вы ввели некорректное описание профиля. "
                + "Допустимо использовать до 256 символов латиницей или кириллицей "
                + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
                + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";
        Assertions.assertEquals(expected, bot.lastText());
        Assertions.assertNull(userRepository.findByUserId(USER_ID).orElseThrow().bio());
    }

    @Test
    @DisplayName("Отправляет ошибку при пустом аргументе")
    void sendsErrorWhenEmptyArgument() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Tony")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = "Вы ввели некорректное описание профиля. "
                + "Допустимо использовать до 256 символов латиницей или кириллицей "
                + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
                + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";
        Assertions.assertEquals(expected, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет ERROR_MESSAGE, если пользователя нет")
    void sendsErrorWhenUserNotFound() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "some bio", bot));

        Assertions.assertEquals(SafeCommandHandler.ERROR_MESSAGE, bot.lastText());
    }
}