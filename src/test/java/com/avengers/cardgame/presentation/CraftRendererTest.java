package com.avengers.cardgame.presentation;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
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

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new TestGameConfig().defaultConfig();

    /** Герой для тестов. */
    private final Hero HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Рендерер крафта. */
    private final CraftRenderer renderer = new CraftRenderer();

    /**
     * Проверяет меню выбора редкости.
     */
    @Test
    @DisplayName("Формирует меню выбора редкости")
    void rendersMenu() {
        Message message = renderer.renderMenu(50, SHARDS_EMOJI, GAME_CONFIG);

        Assertions.assertEquals("🔨 Выбери редкость для крафта. Твой баланс кусочков: 50 🧩.",
                message.text());
        Assertions.assertEquals(2, message.buttons().size());
        Assertions.assertEquals("/craft_make COMMON",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/craft_make LEGENDARY",
                message.buttons().getLast().getLast().callbackData());
        Assertions.assertEquals("⚪ Обычная — 5 🧩",
                message.buttons().getFirst().getFirst().text());
    }

    /**
     * Проверяет сообщение об успешном крафте.
     */
    @Test
    @DisplayName("Формирует сообщение об успешном крафте")
    void rendersCraftSuccess() {
        CraftResult result = new CraftResult(HERO, 5, 45);

        Message message = renderer.renderCraftSuccess(result);

        Assertions.assertEquals("""
                🔨 Крафт успешен! Потрачено кусочков: 5.

                ⚪ Pepper [Обычная]
                ⚔️ ATK: 5 | 🛡 DEF: 15 | ❤️ HP: 20

                Остаток кусочков: 45.""", message.text());
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
}
