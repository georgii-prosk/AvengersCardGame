package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
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
import java.util.List;

/**
 * Тесты для {@link DisassembleCommandHandler}.
 */
class DisassembleCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Эпический герой. */
    private static final Hero EPIC_HERO =
            new Hero(2L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Игровая конфигурация для тестов. */
    private static final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Репозиторий героев. */
    private FakeHeroRepository heroRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    /** Тестируемый обработчик. */
    private DisassembleCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        heroRepository = new FakeHeroRepository();
        heroRepository.save(COMMON_HERO);
        heroRepository.save(EPIC_HERO);
        bot = new FakeBot();
        cardService = new FakeCardService(List.of(COMMON_HERO));
        handler = new DisassembleCommandHandler(cardRepository, heroRepository, cardService, GAME_CONFIG);
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
     * Добавляет карточки героя в коллекцию.
     *
     * @param heroId идентификатор героя
     * @param count  количество карточек
     */
    private void addCards(long heroId, int count) {
        for (int index = 0; index < count; index++) {
            cardRepository.addCard(USER_ID, heroId);
        }
    }

    /**
     * Проверяет пустое сообщение при отсутствии карточек.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточек")
    void sendsEmptyMessage() {
        handler.handle(context(""));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }

    /**
     * Проверяет отображение коллекции для разбора.
     */
    @Test
    @DisplayName("Показывает карточки для разбора")
    void rendersSelection() {
        addCards(COMMON_HERO.id(), 2);

        handler.handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("страница 1/1"));
        Assertions.assertEquals("/disassemble_pick 1",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет, что выбирается только своя карточка.
     */
    @Test
    @DisplayName("Считает только карточки пользователя")
    void ignoresForeignCards() {
        cardRepository.addCard(2L, COMMON_HERO.id());

        handler.handle(context(""));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }

    /**
     * Проверяет некорректный номер страницы.
     */
    @Test
    @DisplayName("Сообщает о некорректной странице")
    void rejectsInvalidPage() {
        handler.handlePage(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный номер"));
    }

    /**
     * Проверяет показ вариантов количества.
     */
    @Test
    @DisplayName("Показывает варианты количества")
    void showsQuantityOptions() {
        addCards(COMMON_HERO.id(), 7);

        handler.handlePick(context("1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("×7"));
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/disassemble_qty 1 all",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет некорректный идентификатор карточки.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе")
    void rejectsInvalidHeroId() {
        handler.handlePick(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный идентификатор"));
    }

    /**
     * Проверяет отсутствующего героя.
     */
    @Test
    @DisplayName("Сообщает об отсутствующем герое")
    void rejectsUnknownHero() {
        handler.handlePick(context("999"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("не существует"));
    }

    /**
     * Проверяет карточку, которой нет в коллекции.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточки в коллекции")
    void rejectsMissingCard() {
        handler.handlePick(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет в твоей коллекции"));
    }

    /**
     * Проверяет подтверждение разбора с количеством «все».
     */
    @Test
    @DisplayName("Показывает подтверждение для всех карточек")
    void showsConfirmationForAll() {
        addCards(COMMON_HERO.id(), 7);

        handler.handleQuantity(context("1 all"));

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
        addCards(COMMON_HERO.id(), 1);

        handler.handleQuantity(context("1 abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректное количество"));
    }

    /**
     * Проверяет запрос больше карточек, чем есть.
     */
    @Test
    @DisplayName("Сообщает о нехватке карточек")
    void rejectsTooManyCards() {
        addCards(COMMON_HERO.id(), 2);

        handler.handleQuantity(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет столько карточек"));
    }

    /**
     * Проверяет успешный разбор.
     */
    @Test
    @DisplayName("Сообщает об успешном разборе")
    void confirmsDisassemble() throws Exception {
        handler.handleConfirm(context("1 2"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Разобрано"));
        Assertions.assertTrue(message.text().contains("×2"));
        Assertions.assertTrue(message.text().contains("Получено кусочков: 2"));
    }

    /**
     * Проверяет обработку ошибки нехватки карточек.
     */
    @Test
    @DisplayName("Обрабатывает ошибку нехватки карточек")
    void handlesNotEnoughCards() throws Exception {
        cardService.disassembleThrows(new NotEnoughCardsException(1L, 5, 2));

        handler.handleConfirm(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("×2"));
    }

    /**
     * Проверяет некорректные параметры подтверждения.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры подтверждения")
    void rejectsInvalidConfirmation() throws Exception {
        handler.handleConfirm(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректное количество"));
    }

    /**
     * Проверяет, что эмодзи кусочков берётся из конфигурации.
     */
    @Test
    @DisplayName("Показывает эмодзи кусочков из конфигурации")
    void showsConfiguredShardsEmoji() {
        handler = new DisassembleCommandHandler(cardRepository, heroRepository, cardService,
                new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "Гири", "🪙", "⭐"));
        addCards(COMMON_HERO.id(), 1);

        handler.handleQuantity(context("1 1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Получишь кусочков: 1 ⭐"));
    }
}
