package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
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
 * Тесты для {@link CraftCommandHandler}.
 */
class CraftCommandHandlerTest {

    /** Идентификатор пользователя, используемый в тестах. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата, используемый в тестах. */
    private static final long CHAT_ID = 10L;

    /** Эпический герой, который может быть получен при крафте. */
    private final Hero epic = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    /** Фейковый бот. */
    private FakeBot bot;

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        cardService = new FakeCardService(List.of(epic));
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
    @DisplayName("MENU показывает все редкости с ценами крафта")
    void menuShowsAllRarities() throws Exception {
        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MENU).handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("🔨 Крафт героя"));
        Assertions.assertTrue(message.text().contains("Обычная — 5 🧩"));
        Assertions.assertTrue(message.text().contains("Редкая — 15 🧩"));
        Assertions.assertTrue(message.text().contains("Эпическая — 40 🧩"));
        Assertions.assertTrue(message.text().contains("Легендарная — 100 🧩"));

        List<String> callbacks = callbacks(message);
        for (HeroRarity rarity : HeroRarity.values()) {
            Assertions.assertTrue(callbacks.contains("/craft_make " + rarity.name()), rarity.name());
        }
        Assertions.assertTrue(callbacks.contains("/cancel"));
    }

    @Test
    @DisplayName("MAKE создаёт карточку и списывает кусочки")
    void makeCraftsCard() throws Exception {
        cardService.setShards(50);

        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE).handle(context("EPIC"));

        Assertions.assertEquals(2, bot.count());
        Assertions.assertEquals("✨ Крафт успешен! Ты получил карточку:", bot.sent().getFirst().message().text());
        Assertions.assertTrue(bot.lastSent().message().text().contains("Iron Man"));
        Assertions.assertEquals(HeroRarity.EPIC, cardService.lastCraftedRarity());
    }

    @Test
    @DisplayName("MAKE сообщает о недостатке кусочков")
    void makeReportsInsufficientShards() throws Exception {
        cardService.setShards(3);

        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE).handle(context("LEGENDARY"));

        Assertions.assertEquals("Недостаточно кусочков: нужно 100 🧩, у тебя 3 🧩.",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("MAKE принимает редкость в нижнем регистре")
    void makeAcceptsLowerCaseRarity() throws Exception {
        cardService.setShards(40);

        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE).handle(context("epic"));

        Assertions.assertEquals(HeroRarity.EPIC, cardService.lastCraftedRarity());
        Assertions.assertEquals(2, bot.count());
    }

    @Test
    @DisplayName("MAKE отклоняет неизвестную редкость")
    void makeRejectsUnknownRarity() throws Exception {
        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE).handle(context("MYTHIC"));

        Assertions.assertEquals("Некорректная редкость карточки.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("MAKE отклоняет пустой аргумент")
    void makeRejectsBlankArgument() throws Exception {
        new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE).handle(context("  "));

        Assertions.assertEquals("Некорректная редкость карточки.", bot.lastSent().message().text());
    }
}
