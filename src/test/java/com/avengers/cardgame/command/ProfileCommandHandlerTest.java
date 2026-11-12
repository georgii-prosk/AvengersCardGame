package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Тесты для {@link ProfileCommandHandler}.
 */
class ProfileCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Игровая конфигурация для тестов. */
    private static final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private ProfileCommandHandler command;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        bot = new FakeBot();
        command = new ProfileCommandHandler(new UserServiceImpl(userRepository), GAME_CONFIG);
    }

    @Test
    @DisplayName("Отправляет полные данные профиля")
    void sendsFullProfile() throws Exception {
        User user = User.of(USER_ID, PlayerName.of("Tony"), new Bio("Genius"),
                100, 7, 5, LocalDateTime.now(), LocalDateTime.now());
        userRepository.save(user);

        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
            Имя: Tony
            Описание: Genius
            Твой баланс: 100 💎.
            Кусочки: 7 🧩.
            В твоей коллекции: 5 карточек.""";
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
                Твой баланс: 0 💎.
                Кусочки: 0 🧩.
                В твоей коллекции: 0 карточек.""";
        Assertions.assertEquals(expected, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Показывает настроенный эмодзи валюты")
    void showsConfiguredCurrencyEmoji() throws Exception {
        GameConfig customConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                "Гири", "🪙");
        ProfileCommandHandler customCommand =
                new ProfileCommandHandler(new UserServiceImpl(userRepository), customConfig);
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                15, 3, 1, LocalDateTime.now(), LocalDateTime.now()));

        customCommand.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(bot.lastSent().message().text().contains("15 🪙"));
    }

    @Test
    @DisplayName("Показывает настроенный эмодзи кусочков")
    void showsConfiguredShardsEmoji() throws Exception {
        GameConfig customConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                "Гири", "🪙", "⭐");
        ProfileCommandHandler customCommand =
                new ProfileCommandHandler(new UserServiceImpl(userRepository), customConfig);
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                15, 3, 1, LocalDateTime.now(), LocalDateTime.now()));

        customCommand.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(bot.lastSent().message().text().contains("3 ⭐"));
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
