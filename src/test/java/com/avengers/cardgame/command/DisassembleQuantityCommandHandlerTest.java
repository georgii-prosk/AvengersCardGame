package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
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
 * Тесты для {@link DisassembleQuantityCommandHandler}.
 */
class DisassembleQuantityCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private DisassembleQuantityCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        bot = new FakeBot();
        handler = new DisassembleQuantityCommandHandler(cardRepository, heroRepository, gameConfig);
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
     * Добавляет карточки обычного героя в коллекцию.
     *
     * @param count количество карточек
     */
    private void addCards(int count) {
        for (int index = 0; index < count; index++) {
            cardRepository.addCard(USER_ID, commonHero.id());
        }
    }

    /**
     * Проверяет подтверждение разбора с количеством «все».
     */
    @Test
    @DisplayName("Показывает подтверждение для всех карточек")
    void showsConfirmationForAll() {
        addCards(7);

        handler.handle(context("1 all"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Разобрать карточек: 7"));
        Assertions.assertTrue(message.text().contains("Получишь кусочков: 7"));
        Assertions.assertEquals("/disassemble_confirm 1 7",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет некорректное количество.
     */
    @Test
    @DisplayName("Сообщает о некорректном количестве")
    void rejectsInvalidQuantity() {
        addCards(1);

        handler.handle(context("1 abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректное количество"));
    }

    /**
     * Проверяет запрос больше карточек, чем есть.
     */
    @Test
    @DisplayName("Сообщает о нехватке карточек")
    void rejectsTooManyCards() {
        addCards(2);

        handler.handle(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет столько карточек"));
    }

    /**
     * Проверяет, что эмодзи кусочков берётся из конфигурации.
     */
    @Test
    @DisplayName("Показывает эмодзи кусочков из конфигурации")
    void showsConfiguredShardsEmoji() {
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        handler = new DisassembleQuantityCommandHandler(cardRepository, heroRepository,
                new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "Гири", "🪙", "⭐"));
        addCards(1);

        handler.handle(context("1 1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Получишь кусочков: 1 ⭐"));
    }
}
