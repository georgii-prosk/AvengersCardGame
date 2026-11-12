package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link CraftCommandHandler}.
 */
class CraftCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private static final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Сервис пользователей. */
    private UserService userService;

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private CraftCommandHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
        cardService = new FakeCardService(List.of(COMMON_HERO));
        bot = new FakeBot();
        handler = new CraftCommandHandler(cardService, userService, GAME_CONFIG);
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                0, 50, 0, LocalDateTime.now(), LocalDateTime.now()));
    }

    /**
     * Создаёт контекст команды.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", argument, bot);
    }

    /**
     * Проверяет меню крафта с балансом кусочков.
     */
    @Test
    @DisplayName("Показывает меню крафта с балансом")
    void showsCraftMenu() throws Exception {
        handler.handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("50"));
        Assertions.assertEquals(2, message.buttons().size());
        Assertions.assertEquals(2, message.buttons().getFirst().size());
        Assertions.assertEquals("/craft_make COMMON",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/craft_make LEGENDARY",
                message.buttons().getLast().getLast().callbackData());
    }

    /**
     * Проверяет успешный крафт.
     */
    @Test
    @DisplayName("Сообщает об успешном крафте")
    void craftsCard() throws Exception {
        handler.handleMake(context("common"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Крафт успешен"));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertTrue(message.text().contains("Потрачено кусочков: 5"));
        Assertions.assertTrue(message.text().contains("Остаток кусочков: 0"));
    }

    /**
     * Проверяет неизвестную редкость.
     */
    @Test
    @DisplayName("Сообщает о неизвестной редкости")
    void rejectsUnknownRarity() throws Exception {
        handler.handleMake(context("mythic"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Неизвестная редкость"));
    }

    /**
     * Проверяет обработку нехватки кусочков.
     */
    @Test
    @DisplayName("Обрабатывает нехватку кусочков")
    void handlesInsufficientShards() throws Exception {
        cardService.craftThrows(new InsufficientShardsException(100, 15));

        handler.handleMake(context("legendary"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Недостаточно кусочков"));
        Assertions.assertTrue(message.text().contains("Нужно: 100"));
        Assertions.assertTrue(message.text().contains("у тебя: 15"));
        Assertions.assertTrue(message.text().contains("Не хватает: 85"));
    }

    /**
     * Проверяет, что команда бросает исключение для незарегистрированного пользователя.
     */
    @Test
    @DisplayName("Бросает исключение для незарегистрированного пользователя")
    void throwsForUnknownUser() {
        Assertions.assertThrows(Exception.class,
                () -> handler.handle(new CommandContext(999L, CHAT_ID, "Unknown", "", bot)));
    }

    /**
     * Проверяет, что эмодзи кусочков берётся из конфигурации.
     */
    @Test
    @DisplayName("Показывает эмодзи кусочков из конфигурации")
    void showsConfiguredShardsEmoji() throws Exception {
        handler = new CraftCommandHandler(cardService, userService,
                new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "Гири", "🪙", "⭐"));

        handler.handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("50 ⭐"));
        Assertions.assertTrue(message.buttons().getFirst().getFirst().text().contains("⭐"));
    }
}
