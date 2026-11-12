package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

/**
 * Тесты для {@link DailyRenderer}.
 */
class DailyRendererTest {

    /** Герой для тестов. */
    private static final Hero IRON_MAN = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Герой для тестов. */
    private static final Hero PEPPER = new Hero(1L, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Рендерер ежедневной награды. */
    private final DailyRenderer renderer = new DailyRenderer();

    /**
     * Проверяет сообщение о недоступной награде.
     */
    @Test
    @DisplayName("Формирует сообщение о кулдауне")
    void rendersCooldown() {
        Message message = renderer.renderCooldown(Duration.ofHours(5).plusMinutes(32));

        Assertions.assertEquals("Ежедневная награда пока недоступна. Загляни через 5 ч 32 мин.",
                message.text());
    }

    /**
     * Проверяет поздравление с наградой.
     */
    @Test
    @DisplayName("Формирует поздравление с наградой")
    void rendersCongrats() {
        Message message = renderer.renderCongrats(2);

        Assertions.assertEquals("🎁 Ежедневная награда! Тебе выпало карточек: 2", message.text());
    }

    /**
     * Проверяет сообщения с выданными карточками.
     */
    @Test
    @DisplayName("Формирует по сообщению на каждую карточку")
    void rendersCards() {
        List<Message> messages = renderer.renderCards(List.of(IRON_MAN, PEPPER));

        Assertions.assertEquals(2, messages.size());
        Assertions.assertTrue(messages.getFirst().text().contains("Iron Man"));
        Assertions.assertTrue(messages.getLast().text().contains("Pepper Potts"));
    }

    /**
     * Проверяет, что без карточек сообщений не отправляется.
     */
    @Test
    @DisplayName("Не формирует сообщений без карточек")
    void rendersNoCards() {
        Assertions.assertTrue(renderer.renderCards(List.of()).isEmpty());
    }
}
