package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ViewCardCallback}.
 */
class ViewCardCallbackTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private InMemoryUserCardRepository cardRepository;
    private InMemoryHeroRepository heroRepository;
    private FakeBot bot;
    private ViewCardCallback callback;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        heroRepository = new InMemoryHeroRepository();
        bot = new FakeBot();
        callback = new ViewCardCallback(heroRepository, cardRepository);
    }

    private void click(String data) throws Exception {
        callback.handle(new CallbackContext(USER_ID, CHAT_ID, data, bot));
    }

    @Test
    @DisplayName("Отправляет полные статы и количество копий")
    void sendsStatsAndCopies() throws Exception {
        heroRepository.save(new Hero(7, "Thor", HeroRarity.EPIC, 30, 20, 100, "Бог грома"));
        cardRepository.addCard(USER_ID, 7);
        cardRepository.addCard(USER_ID, 7);

        click("view_card:7");

        String expected = """
                🟣 Thor
                Редкость: Эпическая
                ⚔️ Атака: 30
                🛡️ Защита: 20
                ❤️ Здоровье: 100
                Бог грома
                В коллекции: ×2
                """;
        Assertions.assertEquals(expected, bot.lastText());
        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Пропускает пустое описание без лишней строки")
    void skipsBlankDescription() throws Exception {
        heroRepository.save(new Hero(7, "Thor", HeroRarity.EPIC, 30, 20, 100, ""));
        cardRepository.addCard(USER_ID, 7);

        click("view_card:7");

        Assertions.assertFalse(bot.lastText().contains("\n\n"));
        Assertions.assertTrue(bot.lastText().endsWith("❤️ Здоровье: 100\nВ коллекции: ×1\n"));
    }

    @Test
    @DisplayName("Сообщает, если героя не существует")
    void reportsUnknownHero() throws Exception {
        click("view_card:999");

        Assertions.assertEquals("Такой карточки не существует.", bot.lastText());
    }

    @Test
    @DisplayName("Сообщает, если карточки нет у пользователя")
    void reportsCardNotOwned() throws Exception {
        heroRepository.save(new Hero(7, "Thor", HeroRarity.EPIC, 30, 20, 100, "d"));

        click("view_card:7");

        Assertions.assertEquals("Этой карточки нет в твоей коллекции.", bot.lastText());
    }

    @Test
    @DisplayName("Игнорирует некорректный идентификатор героя")
    void ignoresInvalidHeroId() throws Exception {
        click("view_card:abc");

        Assertions.assertEquals(0, bot.count());
    }
}
