package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CraftResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link CraftRenderer}.
 */
class CraftRendererTest {

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Герой для тестов. */
    private static final Hero HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Рендерер крафта. */
    private final CraftRenderer renderer = new CraftRenderer();

    /**
     * Проверяет меню выбора редкости.
     */
    @Test
    @DisplayName("Формирует меню выбора редкости")
    void rendersMenu() {
        Message message = renderer.renderMenu(50, SHARDS_EMOJI);

        Assertions.assertTrue(message.text().contains("Твой баланс кусочков: 50 🧩"));
        Assertions.assertEquals(2, message.buttons().size());
        Assertions.assertEquals("/craft_make COMMON",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/craft_make LEGENDARY",
                message.buttons().getLast().getLast().callbackData());
        Assertions.assertTrue(message.buttons().getFirst().getFirst().text().contains("5 🧩"));
    }

    /**
     * Проверяет сообщение об успешном крафте.
     */
    @Test
    @DisplayName("Формирует сообщение об успешном крафте")
    void rendersCraftSuccess() {
        CraftResult result = new CraftResult(HERO, 5, 45);

        Message message = renderer.renderCraftSuccess(result);

        Assertions.assertTrue(message.text().contains("🔨 Крафт успешен! Потрачено кусочков: 5."));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertTrue(message.text().contains("⚔️ ATK: 5 | 🛡 DEF: 15 | ❤️ HP: 20"));
        Assertions.assertTrue(message.text().contains("Остаток кусочков: 45."));
    }

    /**
     * Проверяет сообщение о нехватке кусочков.
     */
    @Test
    @DisplayName("Формирует сообщение о нехватке кусочков")
    void rendersInsufficientShards() {
        Message message = renderer.renderInsufficientShards(100, 15, 85);

        Assertions.assertEquals("❌ Недостаточно кусочков. Нужно: 100, у тебя: 15. Не хватает: 85.",
                message.text());
    }

    /**
     * Проверяет сообщение о неизвестной редкости.
     */
    @Test
    @DisplayName("Формирует сообщение о неизвестной редкости")
    void rendersUnknownRarity() {
        Assertions.assertEquals("Неизвестная редкость крафта.", renderer.renderUnknownRarity().text());
    }
}
