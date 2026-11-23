package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellPriceCommandHandler handler;

    @BeforeEach
    void setUp() {
        FakeHeroRepository heroRepository = new FakeHeroRepository();
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
        Assertions.assertTrue(message.text().contains("Комиссия при продаже: 5%"));
        Assertions.assertTrue(message.text().contains("Ты получишь 95"));
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

        Assertions.assertTrue(bot.lastSent().message().text().contains("от 10 до 200"));
    }

    /**
     * Проверяет некорректные параметры цены.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры цены")
    void rejectsInvalidPriceArguments() {
        handler.handle(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректные параметры"));
    }
}
