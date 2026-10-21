package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.command.InventoryRenderer;
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
 * Тесты для {@link InventoryPageCallback}.
 */
class InventoryPageCallbackTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeBot bot;
    private InventoryPageCallback callback;

    @BeforeEach
    void setUp() {
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        for (long id = 1; id <= 7; id++) {
            heroRepository.save(new Hero(id, "Hero" + id, HeroRarity.COMMON, 1, 1, 1, "d"));
            cardRepository.addCard(USER_ID, id);
        }
        bot = new FakeBot();
        callback = new InventoryPageCallback(new InventoryRenderer(cardRepository, heroRepository));
    }

    @Test
    @DisplayName("Отправляет запрошенную страницу")
    void sendsRequestedPage() {
        callback.handle(new CallbackContext(USER_ID, CHAT_ID, "page:2", bot));

        Assertions.assertTrue(bot.lastText().startsWith("Твоя коллекция (страница 2/2):"));
        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Игнорирует некорректный номер страницы")
    void ignoresInvalidPage() {
        callback.handle(new CallbackContext(USER_ID, CHAT_ID, "page:x", bot));

        Assertions.assertEquals(0, bot.count());
    }
}
