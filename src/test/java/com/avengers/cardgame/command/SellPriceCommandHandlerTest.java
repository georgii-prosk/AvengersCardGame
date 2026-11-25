package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Тесты для {@link SellPriceCommandHandler}.
 */
class SellPriceCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new TestGameConfig().defaultConfig();

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellPriceCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(COMMON_HERO);
        bot = new FakeBot();
        handler = new SellPriceCommandHandler(heroRepository, GAME_CONFIG);
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
     * Проверяет подтверждение лота с расчётом комиссии.
     */
    @Test
    @DisplayName("Показывает подтверждение с комиссией")
    void showsConfirmationWithCommission() {
        handler.handle(context("1 100"));

        Message message = bot.lastSent().message();
        Assertions.assertEquals("""
                🏷 Выставить ⚪ Pepper [Обычная] за 100 💎?
                Комиссия при продаже: 5%. Ты получишь 95 💎.""", message.text());
        Assertions.assertEquals("/sell_confirm 1 100",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет цену вне допустимого диапазона.
     */
    @Test
    @DisplayName("Отклоняет цену вне диапазона")
    void rejectsPriceOutOfRange() {
        handler.handle(context("1 5"));

        Assertions.assertEquals("❌ Цена должна быть от 10 до 200 💎.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет некорректные параметры цены.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры цены")
    void rejectsInvalidPriceArguments() {
        handler.handle(context("1"));

        Assertions.assertEquals("Некорректные параметры цены.",
                bot.lastSent().message().text());
    }
}
