package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link DisassembleCommandHandler}.
 */
class DisassembleCommandHandlerTest {

    /** Идентификатор пользователя, используемый в тестах. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата, используемый в тестах. */
    private static final long CHAT_ID = 10L;

    /** Эпический герой, которым владеет пользователь. */
    private final Hero epic = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    /** Редкий герой, которого у пользователя нет. */
    private final Hero rare = new Hero(7L, "Black Widow", HeroRarity.RARE, 35, 25, 40, "Spy");

    /** Фейковый бот. */
    private FakeBot bot;

    /** Репозиторий героев в памяти. */
    private InMemoryHeroRepository heroRepository;

    /** Репозиторий карточек в памяти. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        heroRepository = new InMemoryHeroRepository();
        heroRepository.save(epic);
        heroRepository.save(rare);
        cardRepository = new InMemoryUserCardRepository();
        cardRepository.addCard(USER_ID, epic.id());
        cardRepository.addCard(USER_ID, epic.id());
        cardService = new FakeCardService(List.of(epic, rare));
    }

    /**
     * Создаёт обработчик нужного шага.
     *
     * @param step шаг обработки
     * @return обработчик
     */
    private DisassembleCommandHandler handler(DisassembleCommandHandler.Step step) {
        return new DisassembleCommandHandler(cardService, cardRepository, heroRepository, step);
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
     * Собирает callback-данные всех кнопок сообщения.
     *
     * @param message сообщение
     * @return список callback-данных
     */
    private List<String> callbacks(Message message) {
        List<String> callbacks = new ArrayList<>();
        for (List<Button> row : message.buttons()) {
            for (Button button : row) {
                callbacks.add(button.callbackData());
            }
        }
        return callbacks;
    }

    @Test
    @DisplayName("START показывает коллекцию с кнопками выбора")
    void startShowsInventory() throws Exception {
        handler(DisassembleCommandHandler.Step.START).handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("♻️ Выбери карточку для разбора (страница 1/1):"));
        Assertions.assertTrue(message.text().contains("Iron Man"));
        Assertions.assertTrue(callbacks(message).contains("/disassemble_pick 9"));
    }

    @Test
    @DisplayName("START для пустой коллекции показывает заглушку")
    void startShowsEmptyMessage() throws Exception {
        InMemoryUserCardRepository emptyRepository = new InMemoryUserCardRepository();
        DisassembleCommandHandler handler = new DisassembleCommandHandler(
                cardService, emptyRepository, heroRepository, DisassembleCommandHandler.Step.START);

        handler.handle(context(""));

        Assertions.assertEquals(
                "У тебя пока нет карточек. Забери ежедневную награду командой /daily!",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PICK показывает варианты количества для своей карточки")
    void pickShowsQuantityOptions() throws Exception {
        handler(DisassembleCommandHandler.Step.PICK).handle(context("9"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("За одну карточку: +8 🧩"));
        Assertions.assertTrue(message.text().contains("В коллекции: ×2"));
        List<String> callbacks = callbacks(message);
        Assertions.assertTrue(callbacks.contains("/disassemble_qty 9 1"));
        Assertions.assertTrue(callbacks.contains("/disassemble_qty 9 2"));
        Assertions.assertTrue(callbacks.contains("/cancel"));
    }

    @Test
    @DisplayName("PICK отклоняет неизвестного героя")
    void pickRejectsUnknownHero() throws Exception {
        handler(DisassembleCommandHandler.Step.PICK).handle(context("999"));

        Assertions.assertEquals("Такой карточки не существует.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PICK отклоняет карточку, которой нет в коллекции")
    void pickRejectsNotOwnedCard() throws Exception {
        handler(DisassembleCommandHandler.Step.PICK).handle(context("7"));

        Assertions.assertEquals("У тебя нет этой карточки.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PICK отклоняет некорректный аргумент")
    void pickRejectsInvalidArgument() throws Exception {
        handler(DisassembleCommandHandler.Step.PICK).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("QUANTITY показывает подтверждение с итогом кусочков")
    void quantityShowsConfirmation() throws Exception {
        handler(DisassembleCommandHandler.Step.QUANTITY).handle(context("9 2"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Подтверди разбор"));
        Assertions.assertTrue(message.text().contains("+16 🧩"));
        Assertions.assertTrue(callbacks(message).contains("/disassemble_confirm 9 2"));
    }

    @Test
    @DisplayName("QUANTITY отклоняет запрос больше доступного")
    void quantityRejectsTooManyCards() throws Exception {
        handler(DisassembleCommandHandler.Step.QUANTITY).handle(context("9 3"));

        Assertions.assertEquals("У тебя только 2 таких карточек.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("QUANTITY отклоняет нулевое количество")
    void quantityRejectsZero() throws Exception {
        handler(DisassembleCommandHandler.Step.QUANTITY).handle(context("9 0"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("QUANTITY отклоняет некорректный формат")
    void quantityRejectsInvalidFormat() throws Exception {
        handler(DisassembleCommandHandler.Step.QUANTITY).handle(context("9"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("QUANTITY отклоняет неизвестного героя")
    void quantityRejectsUnknownHero() throws Exception {
        handler(DisassembleCommandHandler.Step.QUANTITY).handle(context("999 1"));

        Assertions.assertEquals("Такой карточки не существует.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("CONFIRM выполняет разбор и сообщает результат")
    void confirmDisassemblesCards() throws Exception {
        handler(DisassembleCommandHandler.Step.CONFIRM).handle(context("9 2"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Разобрано: Iron Man ×2"));
        Assertions.assertTrue(message.text().contains("+16 🧩"));
        Assertions.assertEquals(epic.id(), cardService.lastDisassembledHeroId());
        Assertions.assertEquals(2, cardService.lastDisassembledCount());
    }

    @Test
    @DisplayName("CONFIRM сообщает о недостатке карточек")
    void confirmReportsNotEnoughCards() throws Exception {
        FakeCardService emptyService = new FakeCardService(List.of());
        DisassembleCommandHandler handler = new DisassembleCommandHandler(
                emptyService, cardRepository, heroRepository, DisassembleCommandHandler.Step.CONFIRM);

        handler.handle(context("9 1"));

        Assertions.assertEquals("Недостаточно карточек: доступно 0.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("CONFIRM отклоняет некорректный формат и ноль")
    void confirmRejectsInvalidArguments() throws Exception {
        handler(DisassembleCommandHandler.Step.CONFIRM).handle(context("abc"));
        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());

        handler(DisassembleCommandHandler.Step.CONFIRM).handle(context("9 0"));
        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PAGE отклоняет некорректный номер страницы")
    void pageRejectsInvalidNumber() throws Exception {
        handler(DisassembleCommandHandler.Step.PAGE).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PAGE вне диапазона приводит к существующей странице")
    void pageClampsOutOfRange() throws Exception {
        handler(DisassembleCommandHandler.Step.PAGE).handle(context("99"));

        Assertions.assertTrue(bot.lastSent().message().text()
                .startsWith("♻️ Выбери карточку для разбора (страница 1/1):"));
    }
}
