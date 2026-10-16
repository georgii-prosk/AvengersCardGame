package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ProfileCommand}.
 */
class ProfileCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeUserRepository userRepository;
    private FakeBot bot;
    private ProfileCommand command;

    @BeforeEach
    void setUp(){
        userRepository = new FakeUserRepository();
        bot = new FakeBot();
        command = new ProfileCommand(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Отправляет полные данные профиля")
    void sendsFullProfile() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Tony"), new Bio("Genius"),
                100, 5, null, null));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
                Имя: Tony
                Описание: Genius
                Твой баланс: 100 щитов
                В твоей коллекции: 5 карточек
                """;
        Assertions.assertEquals(expected, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет «не задано» для пустого bio")
    void sendsNotSetForEmptyBio() throws Exception {
        userRepository.save(new User(USER_ID, new PlayerName("Tony")));

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
                Имя: Tony
                Описание: не задано
                Твой баланс: 0 щитов
                В твоей коллекции: 0 карточек
                """;
        Assertions.assertEquals(expected, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет сообщение об ошибке, если пользователя нет")
    void sendsErrorWhenUserNotFound() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals(SafeCommandHandler.ERROR_MESSAGE, bot.lastText());
    }
}