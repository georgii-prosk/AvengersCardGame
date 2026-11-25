package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.DisassembleResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Тесты для {@link DisassembleRenderer}.
 */
class DisassembleRendererTest {

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer renderer = new DisassembleRenderer();

    /**
     * Проверяет страницу коллекции для разбора.
     */
    @Test
    @DisplayName("Показывает страницу карточек для разбора")
    void rendersSelection() {
        Message message = renderer.renderSelection(
                List.of(new InventoryItem(COMMON_HERO, 3)), 1, SHARDS_EMOJI);

        Assertions.assertEquals("""
                🧩 Выбери карточку для разбора (страница 1/1):
                ⚪ Pepper — Обычная ×3""", message.text());
        Assertions.assertEquals("/disassemble_pick 1",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет сообщение при отсутствии карточек.
     */
    @Test
    @DisplayName("Показывает сообщение при отсутствии карточек")
    void rendersEmptySelection() {
        Message message = renderer.renderSelection(List.of(), 1, SHARDS_EMOJI);

        Assertions.assertEquals("У тебя пока нет карточек для разбора.", message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    /**
     * Проверяет варианты количества для разбора.
     */
    @Test
    @DisplayName("Показывает варианты количества")
    void rendersQuantity() {
        Message message = renderer.renderQuantity(COMMON_HERO, 7);

        Assertions.assertEquals(String.join(System.lineSeparator(),
                "⚪ Pepper [Обычная] ×7",
                "Сколько карточек разобрать?"), message.text());
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/disassemble_qty 1 all",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет подтверждение разбора.
     */
    @Test
    @DisplayName("Показывает подтверждение разбора")
    void rendersConfirmation() {
        Message message = renderer.renderConfirmation(COMMON_HERO, 7, 5, 25, SHARDS_EMOJI);

        Assertions.assertEquals(String.join(System.lineSeparator(),
                "⚪ Pepper [Обычная] ×7",
                "Разобрать карточек: 5?",
                "Получишь кусочков: 25 🧩."), message.text());
        Assertions.assertEquals("/disassemble_confirm 1 5",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет результат разбора.
     */
    @Test
    @DisplayName("Показывает результат разбора")
    void rendersResult() {
        DisassembleResult result = new DisassembleResult(COMMON_HERO, 2, 10, 40);

        Message message = renderer.renderResult(result, SHARDS_EMOJI);

        Assertions.assertEquals("""
                🧩 Разобрано: ⚪ Pepper [Обычная] ×2.
                Получено кусочков: 10.
                Твой баланс кусочков: 40.""", message.text());
    }

    /**
     * Проверяет сообщение о нехватке карточек.
     */
    @Test
    @DisplayName("Показывает сообщение о нехватке карточек")
    void rendersNotEnoughCards() {
        Message message = renderer.renderNotEnoughCards("Pepper", 2);

        Assertions.assertEquals("❌ У тебя нет столько карточек Pepper. В коллекции: ×2.",
                message.text());
    }

    /**
     * Проверяет сообщения об ошибках разбора.
     */
    @Test
    @DisplayName("Показывает сообщения об ошибках разбора")
    void rendersErrorMessages() {
        Assertions.assertEquals("Некорректный номер страницы.", renderer.renderInvalidPage().text());
        Assertions.assertEquals("Некорректный идентификатор карточки.", renderer.renderInvalidHeroId().text());
        Assertions.assertEquals("Такой карточки не существует.", renderer.renderUnknownHero().text());
        Assertions.assertEquals("Этой карточки нет в твоей коллекции.",
                renderer.renderCardNotInCollection().text());
        Assertions.assertEquals("Некорректное количество карточек.", renderer.renderInvalidQuantity().text());
    }
}
