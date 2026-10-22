package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ProfileCommandHandler}.
 */
class ProfileCommandHandlerTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private InMemoryUserRepository userRepository;
    private InMemoryUserCardRepository cardRepository;
    private FakeBot bot;
    private ProfileCommandHandler command;

    @BeforeEach
    void setUp(){
        userRepository = new InMemoryUserRepository();
        cardRepository = new InMemoryUserCardRepository();
        bot = new FakeBot();
        command = new ProfileCommandHandler(new UserServiceImpl(userRepository), cardRepository);
    }

    @Test
    @DisplayName("Отправляет полные данные профиля")
    void sendsFullProfile() throws Exception {
        User user = User.of(USER_ID, PlayerName.of("Tony"), new Bio("Genius"),
                100, 0, null, null);   // cardCount больше не используется
        userRepository.save(user);
        for (int i = 0; i < 5; i++) {
            cardRepository.addCard(USER_ID, 1L);
        }

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
            Имя: Tony
            Описание: Genius
            Твой баланс: 100 щитов
            В твоей коллекции: 5 карточек
            """;
        Assertions.assertEquals(expected, bot.lastSent().message().text());
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
        Assertions.assertEquals(expected, bot.lastSent().message().text());
    }

    /**
     * Проверяет, что команда бросает UserNotFoundException, если пользователя нет.
     */
    @Test
    @DisplayName("Бросает UserNotFoundException, если пользователя нет")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot)));
    }
}