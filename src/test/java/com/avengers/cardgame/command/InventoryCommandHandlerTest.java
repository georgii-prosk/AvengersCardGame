package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.Button;
import com.avengers.cardgame.service.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Тесты для {@link InventoryCommandHandler} и {@link InventoryRenderer}.
 */
class InventoryCommandHandlerTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private InMemoryUserCardRepository cardRepository;
    private InMemoryHeroRepository heroRepository;
    private FakeBot bot;
    private InventoryRenderer renderer;
    private InventoryCommandHandler command;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        heroRepository = new InMemoryHeroRepository();
        bot = new FakeBot();
        renderer = new InventoryRenderer(cardRepository, heroRepository);
        command = new InventoryCommandHandler(renderer);
    }

    private void run() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));
    }

    private void addHero(long id, String name, HeroRarity rarity) {
        heroRepository.save(new Hero(id, name, rarity, 10, 5, 20, "desc"));
    }

    @Test
    @DisplayName("Сообщает, что карточек нет, и не добавляет кнопок")
    void sendsEmptyMessageWhenNoCards() {
        run();

        Assertions.assertEquals(1, bot.count());
        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
        Assertions.assertTrue(bot.lastSent().message().buttons().isEmpty());
    }

    @Test
    @DisplayName("Группирует повторные карточки как ×N")
    void groupsDuplicatesAsCount() {
        addHero(1, "Thor", HeroRarity.EPIC);
        cardRepository.addCard(USER_ID, 1);
        cardRepository.addCard(USER_ID, 1);
        cardRepository.addCard(USER_ID, 1);

        run();

        Assertions.assertTrue(bot.lastSent().message().text().contains("Thor"));
        Assertions.assertTrue(bot.lastSent().message().text().contains("×3"));
        Assertions.assertTrue(bot.lastSent().message().text().contains("Эпическая"));
    }

    @Test
    @DisplayName("Отправляет одно сообщение со страницей 1/1 и кнопкой на каждую карточку")
    void sendsMessageWithCardButtons() {
        addHero(1, "Thor", HeroRarity.EPIC);
        addHero(2, "Hulk", HeroRarity.RARE);
        cardRepository.addCard(USER_ID, 1);
        cardRepository.addCard(USER_ID, 2);

        run();

        Message message = bot.lastSent().message();
        Assertions.assertEquals(1, bot.count());
        Assertions.assertTrue(message.text().startsWith("Твоя коллекция (страница 1/1):"));
        Assertions.assertEquals(2, message.buttons().size());
        List<String> data = message.buttons().stream()
                .map(row -> row.getFirst().callbackData())
                .toList();
        Assertions.assertEquals(List.of("view_card:1", "view_card:2"), data);
    }

    @Test
    @DisplayName("Сортирует: сначала редкие, затем по имени")
    void sortsByRarityThenName() {
        addHero(1, "Zed", HeroRarity.COMMON);
        addHero(2, "Alpha", HeroRarity.COMMON);
        addHero(3, "Legend", HeroRarity.LEGENDARY);
        cardRepository.addCard(USER_ID, 1);
        cardRepository.addCard(USER_ID, 2);
        cardRepository.addCard(USER_ID, 3);

        run();

        List<String> data = bot.lastSent().message().buttons().stream()
                .map(row -> row.getFirst().callbackData())
                .toList();
        Assertions.assertEquals(List.of("view_card:3", "view_card:2", "view_card:1"), data);
    }

    @Test
    @DisplayName("Разбивает коллекцию на страницы по 5 и добавляет кнопку «Вперёд»")
    void paginatesByFive() {
        for (long id = 1; id <= 7; id++) {
            addHero(id, "Hero" + id, HeroRarity.COMMON);
            cardRepository.addCard(USER_ID, id);
        }

        run();

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("Твоя коллекция (страница 1/2):"));
        // 5 рядов карточек + ряд навигации
        Assertions.assertEquals(6, message.buttons().size());
        List<Button> navigation = message.buttons().getLast();
        Assertions.assertEquals(1, navigation.size());
        Assertions.assertEquals("page:2", navigation.getFirst().callbackData());
    }

    @Test
    @DisplayName("На второй странице есть «Назад» и остаток карточек")
    void secondPageHasBackButton() {
        for (long id = 1; id <= 7; id++) {
            addHero(id, "Hero" + id, HeroRarity.COMMON);
            cardRepository.addCard(USER_ID, id);
        }

        Message message = renderer.render(USER_ID, 2);

        Assertions.assertTrue(message.text().startsWith("Твоя коллекция (страница 2/2):"));
        // 2 карточки + ряд навигации
        Assertions.assertEquals(3, message.buttons().size());
        List<Button> navigation = message.buttons().getLast();
        Assertions.assertEquals(1, navigation.size());
        Assertions.assertEquals("page:1", navigation.getFirst().callbackData());
    }

    @Test
    @DisplayName("Номер страницы вне диапазона приводится к существующей")
    void clampsPageNumber() {
        addHero(1, "Thor", HeroRarity.EPIC);
        cardRepository.addCard(USER_ID, 1);

        Assertions.assertTrue(renderer.render(USER_ID, 99).text().contains("страница 1/1"));
        Assertions.assertTrue(renderer.render(USER_ID, -5).text().contains("страница 1/1"));
    }

    @Test
    @DisplayName("Не показывает чужие карточки")
    void ignoresOtherUsersCards() {
        addHero(1, "Thor", HeroRarity.EPIC);
        cardRepository.addCard(2L, 1);

        run();

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }

    @Test
    @DisplayName("Отправляет сообщение в нужный чат")
    void sendsToCorrectChat() {
        run();

        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());
    }
}
