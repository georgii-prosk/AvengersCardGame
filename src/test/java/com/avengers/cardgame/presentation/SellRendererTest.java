package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketListing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link SellRenderer}.
 */
class SellRendererTest {

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Эмодзи валюты. */
    private static final String CURRENCY = "💎";

    /** Рендерер выставления лота. */
    private final SellRenderer renderer = new SellRenderer();

    /**
     * Проверяет страницу коллекции для продажи.
     */
    @Test
    @DisplayName("Показывает страницу карточек для продажи")
    void rendersSelection() {
        Message message = renderer.renderSelection(List.of(new InventoryItem(COMMON_HERO, 2)), 1);

        Assertions.assertTrue(message.text().startsWith("🏷 Выбери карточку для продажи (страница 1/1):"));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertEquals("/sell_pick 1",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет сообщение при отсутствии карточек.
     */
    @Test
    @DisplayName("Показывает сообщение при отсутствии карточек")
    void rendersEmptySelection() {
        Message message = renderer.renderSelection(List.of(), 1);

        Assertions.assertEquals("У тебя пока нет карточек для продажи.", message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    /**
     * Проверяет варианты цены.
     */
    @Test
    @DisplayName("Показывает варианты цены")
    void rendersPrice() {
        Message message = renderer.renderPrice(COMMON_HERO, 10, 100, CURRENCY);

        Assertions.assertTrue(message.text().contains("Выбери цену. Допустимо от 10 до 100 💎."));
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/sell_price 1 10",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/sell_price 1 55",
                message.buttons().getFirst().get(1).callbackData());
        Assertions.assertEquals("/sell_price 1 100",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет подтверждение выставления лота.
     */
    @Test
    @DisplayName("Показывает подтверждение выставления лота")
    void rendersConfirmation() {
        Message message = renderer.renderConfirmation(COMMON_HERO, 100, 95, 5, CURRENCY);

        Assertions.assertTrue(message.text().contains("🏷 Выставить"));
        Assertions.assertTrue(message.text().contains("за 100 💎?"));
        Assertions.assertTrue(message.text().contains("Комиссия при продаже: 5%. Ты получишь 95 💎."));
        Assertions.assertEquals("/sell_confirm 1 100",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет сообщение о созданном лоте.
     */
    @Test
    @DisplayName("Показывает сообщение о созданном лоте")
    void rendersCreated() {
        MarketListing listing = new MarketListing(7L, 2L, 1L, 100, LocalDateTime.now());

        Message message = renderer.renderCreated(listing, COMMON_HERO, CURRENCY);

        Assertions.assertTrue(message.text().contains("✅ Лот #7 создан:"));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertTrue(message.text().contains("за 100 💎"));
    }

    /**
     * Проверяет сообщение о созданном лоте без данных героя.
     */
    @Test
    @DisplayName("Показывает сообщение о созданном лоте без героя")
    void rendersCreatedWithoutHero() {
        MarketListing listing = new MarketListing(7L, 2L, 1L, 100, LocalDateTime.now());

        Message message = renderer.renderCreated(listing, null, CURRENCY);

        Assertions.assertEquals("✅ Лот #7 создан за 100 💎.", message.text());
    }

    /**
     * Проверяет сообщения об ошибках выставления лота.
     */
    @Test
    @DisplayName("Показывает сообщения об ошибках выставления лота")
    void rendersErrorMessages() {
        Assertions.assertEquals("Некорректный номер страницы.", renderer.renderInvalidPage().text());
        Assertions.assertEquals("Некорректный идентификатор карточки.", renderer.renderInvalidHeroId().text());
        Assertions.assertEquals("Такой карточки не существует.", renderer.renderUnknownHero().text());
        Assertions.assertEquals("Этой карточки нет в твоей коллекции.",
                renderer.renderCardNotInCollection().text());
        Assertions.assertEquals("Некорректные параметры цены.", renderer.renderInvalidPriceArguments().text());
        Assertions.assertEquals("Некорректные параметры лота.", renderer.renderInvalidListingArguments().text());
        Assertions.assertEquals("❌ Цена должна быть от 10 до 100 💎.",
                renderer.renderInvalidPrice(10, 100, CURRENCY).text());
        Assertions.assertEquals("❌ У тебя нет этой карточки в коллекции.", renderer.renderMissingCard().text());
    }
}
