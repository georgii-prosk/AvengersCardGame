package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Тесты для {@link DisassemblePageCommandHandler}.
 */
class DisassemblePageCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private DisassemblePageCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        bot = new FakeBot();
        handler = new DisassemblePageCommandHandler(cardRepository, heroRepository, gameConfig);
    }

    /**
     * Создаёт контекст команды.
     *
     * @return контекст
     */
    private CommandContext context() {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", "abc", bot);
    }

    /**
     * Проверяет некорректный номер страницы.
     */
    @Test
    @DisplayName("Сообщает о некорректной странице")
    void rejectsInvalidPage() {
        handler.handle(context());

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный номер"));
    }
}
