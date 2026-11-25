package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link CraftCommandHandler}.
 */
class CraftCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new TestGameConfig().defaultConfig();

    /** Сервис пользователей. */
    private UserService userService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private CraftCommandHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
        bot = new FakeBot();
        handler = new CraftCommandHandler(userService, gameConfig);
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                0, 50, 0, LocalDateTime.now(), LocalDateTime.now()));
    }

    /**
     * Создаёт контекст команды.
     *
     * @return контекст
     */
    private CommandContext context() {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot);
    }

    /**
     * Проверяет меню крафта с балансом кусочков.
     */
    @Test
    @DisplayName("Показывает меню крафта с балансом")
    void showsCraftMenu() throws Exception {
        handler.handle(context());

        Message message = bot.lastSent().message();
        Assertions.assertEquals("🔨 Выбери редкость для крафта. Твой баланс кусочков: 50 🧩.",
                message.text());
        Assertions.assertEquals(2, message.buttons().size());
        Assertions.assertEquals(2, message.buttons().getFirst().size());
        Assertions.assertEquals("/craft_make COMMON",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/craft_make LEGENDARY",
                message.buttons().getLast().getLast().callbackData());
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
        handler = new CraftCommandHandler(userService,
                new TestGameConfig().withCurrency("Гири", "🪙", "⭐"));

        handler.handle(context());

        Message message = bot.lastSent().message();
        Assertions.assertEquals("🔨 Выбери редкость для крафта. Твой баланс кусочков: 50 ⭐.",
                message.text());
        Assertions.assertEquals("⚪ Обычная — 5 ⭐",
                message.buttons().getFirst().getFirst().text());
    }
}
