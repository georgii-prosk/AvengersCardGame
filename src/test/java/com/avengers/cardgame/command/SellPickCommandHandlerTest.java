package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
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
 * Тесты для {@link SellPickCommandHandler}.
 */
class SellPickCommandHandlerTest {

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

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellPickCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        FakeHeroRepository heroRepository = new FakeHeroRepository();
        heroRepository.save(COMMON_HERO);
        bot = new FakeBot();
        handler = new SellPickCommandHandler(cardRepository, heroRepository, GAME_CONFIG);
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
     * Проверяет показ вариантов цены.
     */
    @Test
    @DisplayName("Показывает варианты цены")
    void showsPriceOptions() {
        cardRepository.addCard(USER_ID, COMMON_HERO.id());

        handler.handle(context("1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("от 10 до 200"));
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/sell_price 1 10",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/sell_price 1 105",
                message.buttons().getFirst().get(1).callbackData());
        Assertions.assertEquals("/sell_price 1 200",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет некорректный идентификатор карточки.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе")
    void rejectsInvalidHeroId() {
        handler.handle(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный идентификатор"));
    }

    /**
     * Проверяет отсутствующего героя.
     */
    @Test
    @DisplayName("Сообщает об отсутствующем герое")
    void rejectsUnknownHero() {
        handler.handle(context("999"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("не существует"));
    }

    /**
     * Проверяет карточку, которой нет в коллекции.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточки в коллекции")
    void rejectsMissingCard() {
        handler.handle(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет в твоей коллекции"));
    }
}
