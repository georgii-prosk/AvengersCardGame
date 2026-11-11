package com.avengers.cardgame.presentation;

import com.avengers.cardgame.support.NullParameterAssertion;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link MarketRenderer}.
 */
class MarketRendererTest {

    /** Время выставления лота, используемое в тестах. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Эмодзи валюты, используемый в тестах. */
    private static final String CURRENCY_EMOJI = "💎";

    /** Количество лотов на двух страницах рынка. */
    private static final int ITEM_COUNT = 6;

    /** Рендерер, который тестируется. */
    private final MarketRenderer renderer = new MarketRenderer();

    /**
     * Создаёт элемент рынка с заданными идентификатором и ценой.
     *
     * @param id    идентификатор лота
     * @param price цена лота
     * @return элемент рынка
     */
    private MarketItem item(long id, int price) {
        Hero hero = new Hero(id, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");
        MarketListing listing = new MarketListing(id, 1L, id, price, CREATED_AT);
        return new MarketItem(listing, hero, "Tony");
    }

    /**
     * Создаёт список лотов для двух страниц рынка.
     *
     * @return список лотов
     */
    private List<MarketItem> items() {
        List<MarketItem> items = new ArrayList<>();
        for (long id = 1; id <= ITEM_COUNT; id++) {
            items.add(item(id, 500));
        }
        return items;
    }

    @Test
    @DisplayName("Пустой рынок отображает заглушку без кнопок")
    void emptyMarketShowsPlaceholder() {
        Message message = renderer.render(List.of(), 1, CURRENCY_EMOJI);

        Assertions.assertEquals("На рынке пока нет лотов. Стань первым продавцом: /sell", message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    @Test
    @DisplayName("Пустой список собственных лотов отображает заглушку")
    void emptyOwnListingsShowPlaceholder() {
        Message message = renderer.renderOwn(List.of(), 1, CURRENCY_EMOJI);

        Assertions.assertEquals("У тебя нет активных лотов. Выставь карточку командой /sell.", message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    @Test
    @DisplayName("Рынок показывает лот, цену и кнопку покупки")
    void marketPageShowsLotAndBuyButton() {
        Message message = renderer.render(List.of(item(1L, 500)), 1, CURRENCY_EMOJI);

        Assertions.assertEquals("""
                🏪 Рынок (страница 1/1):
                🟣 Iron Man — Эпическая | 500 💎 | Продавец: Tony""", message.text());
        Assertions.assertEquals(1, message.buttons().size());
        Button button = message.buttons().getFirst().getFirst();
        Assertions.assertEquals("Купить за 500 💎", button.text());
        Assertions.assertEquals("/market_buy 1", button.callbackData());
    }

    @Test
    @DisplayName("Собственные лоты показывают кнопку снятия")
    void ownPageShowsCancelButton() {
        Message message = renderer.renderOwn(List.of(item(3L, 700)), 1, CURRENCY_EMOJI);

        Assertions.assertTrue(message.text().startsWith("📦 Твои лоты (страница 1/1):"));
        Button button = message.buttons().getFirst().getFirst();
        Assertions.assertEquals("Снять лот", button.text());
        Assertions.assertEquals("/market_cancel 3", button.callbackData());
    }

    @Test
    @DisplayName("На первой странице есть кнопка перехода вперёд")
    void firstPageHasNextButton() {
        Message message = renderer.render(items(), 1, CURRENCY_EMOJI);

        Assertions.assertTrue(message.text().startsWith("🏪 Рынок (страница 1/2):"));
        Assertions.assertEquals(6, message.buttons().size());
        Button next = message.buttons().getLast().getFirst();
        Assertions.assertEquals("Вперёд ▶️", next.text());
        Assertions.assertEquals("/market_page 2", next.callbackData());
    }

    @Test
    @DisplayName("На последней странице есть кнопка перехода назад")
    void lastPageHasPreviousButton() {
        Message message = renderer.render(items(), 2, CURRENCY_EMOJI);

        Assertions.assertTrue(message.text().startsWith("🏪 Рынок (страница 2/2):"));
        Assertions.assertEquals(2, message.buttons().size());
        Button previous = message.buttons().getLast().getFirst();
        Assertions.assertEquals("◀️ Назад", previous.text());
        Assertions.assertEquals("/market_page 1", previous.callbackData());
    }

    @Test
    @DisplayName("Номер страницы приводится к допустимому диапазону")
    void pageIsClamped() {
        Message tooLow = renderer.render(items(), 0, CURRENCY_EMOJI);
        Message tooHigh = renderer.render(items(), 99, CURRENCY_EMOJI);

        Assertions.assertTrue(tooLow.text().startsWith("🏪 Рынок (страница 1/2):"));
        Assertions.assertTrue(tooHigh.text().startsWith("🏪 Рынок (страница 2/2):"));
    }

    @Test
    @DisplayName("Собственные лоты используют собственную команду пагинации")
    void ownPageUsesOwnPageCommand() {
        Message message = renderer.renderOwn(items(), 1, CURRENCY_EMOJI);

        Button next = message.buttons().getLast().getFirst();
        Assertions.assertEquals("/market_mine_page 2", next.callbackData());
    }

    @Test
    @DisplayName("Null вместо эмодзи валюты отклоняется")
    @SuppressWarnings("DataFlowIssue")
    void nullCurrencyEmojiIsRejected() {
        NullParameterAssertion.assertRejected(
                () -> renderer.render(List.of(), 1, null));
        NullParameterAssertion.assertRejected(
                () -> renderer.renderOwn(List.of(), 1, null));
    }

    @Test
    @DisplayName("Null вместо списка лотов отклоняется")
    @SuppressWarnings("DataFlowIssue")
    void nullItemsAreRejected() {
        NullParameterAssertion.assertRejected(
                () -> renderer.render(null, 1, CURRENCY_EMOJI));
        NullParameterAssertion.assertRejected(
                () -> renderer.renderOwn(null, 1, CURRENCY_EMOJI));
    }
}
