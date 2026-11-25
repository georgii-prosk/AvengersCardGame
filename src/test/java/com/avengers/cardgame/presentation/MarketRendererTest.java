package com.avengers.cardgame.presentation;

import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link MarketRenderer}.
 */
class MarketRendererTest {

    /** Эмодзи валюты для тестов. */
    private static final String CURRENCY = "💎";

    /** Время создания лота для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Герой для тестов. */
    private static final Hero HERO = new Hero(3L, "Hero", HeroRarity.EPIC, 10, 5, 20, "desc");

    /** Рендерер рынка. */
    private MarketRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new MarketRenderer();
    }

    /**
     * Создаёт объявление рынка.
     *
     * @param id     идентификатор лота
     * @param rarity редкость героя
     * @param price  цена
     * @return объявление рынка
     */
    private MarketItem item(long id, HeroRarity rarity, int price) {
        Hero hero = new Hero(id + 100, "Hero" + id, rarity, 10, 5, 20, "desc");
        MarketListing listing = new MarketListing(id, 2L, hero.id(), price, CREATED_AT);
        return new MarketItem(listing, hero, "Seller");
    }

    /**
     * Создаёт список объявлений из семи штук.
     *
     * @return список объявлений
     */
    private List<MarketItem> items() {
        List<MarketItem> result = new ArrayList<>();
        for (long id = 1; id <= 7; id++) {
            result.add(item(id, HeroRarity.COMMON, 100));
        }
        return result;
    }

    /**
     * Проверяет сообщение для пустого рынка.
     */
    @Test
    @DisplayName("Сообщает, что рынок пуст")
    void rendersEmptyMarket() {
        Message message = renderer.renderMarket(List.of(), 1, CURRENCY, MarketRenderer.ALL_FILTER);

        Assertions.assertEquals("Сейчас на рынке нет лотов. Будь первым — выставь карточку через /sell!",
                message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    /**
     * Проверяет отображение одного лота.
     */
    @Test
    @DisplayName("Отображает лот с ценой и продавцом")
    void rendersSingleListing() {
        Message message = renderer.renderMarket(
                List.of(item(7L, HeroRarity.EPIC, 500)), 1, CURRENCY, MarketRenderer.ALL_FILTER);

        Assertions.assertEquals("""
                🏪 Рынок (страница 1/1):
                #7 🟣 Hero7 [Эпическая] — 500 💎 (продавец: Seller)""", message.text());
        Assertions.assertEquals("/market_buy 7", message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет, что на странице не больше пяти лотов.
     */
    @Test
    @DisplayName("Показывает не больше пяти лотов на странице")
    void limitsPageToFiveItems() {
        Message message = renderer.renderMarket(items(), 1, CURRENCY, MarketRenderer.ALL_FILTER);

        Assertions.assertTrue(message.text().startsWith("🏪 Рынок (страница 1/2):"));
        List<Button> buyButtons = message.buttons().getFirst();
        Assertions.assertEquals(5, buyButtons.size());
        Assertions.assertEquals("/market_buy 1", buyButtons.getFirst().callbackData());
        Assertions.assertEquals("/market_buy 5", buyButtons.getLast().callbackData());
    }

    /**
     * Проверяет, что на первой странице есть кнопка «Вперёд».
     */
    @Test
    @DisplayName("Добавляет кнопку «Вперёд» на первой странице")
    void addsNextButtonOnFirstPage() {
        Message message = renderer.renderMarket(items(), 1, CURRENCY, MarketRenderer.ALL_FILTER);

        List<Button> navigation = message.buttons().getLast();
        Assertions.assertEquals(1, navigation.size());
        Assertions.assertEquals("/market_page ALL 2", navigation.getFirst().callbackData());
    }

    /**
     * Проверяет, что на второй странице есть кнопка «Назад».
     */
    @Test
    @DisplayName("Добавляет кнопку «Назад» на второй странице")
    void addsPreviousButtonOnSecondPage() {
        Message message = renderer.renderMarket(items(), 2, CURRENCY, MarketRenderer.ALL_FILTER);

        Assertions.assertTrue(message.text().startsWith("🏪 Рынок (страница 2/2):"));
        Assertions.assertEquals(2, message.buttons().getFirst().size());
        List<Button> navigation = message.buttons().getLast();
        Assertions.assertEquals(1, navigation.size());
        Assertions.assertEquals("/market_page ALL 1", navigation.getFirst().callbackData());
    }

    /**
     * Проверяет, что номер страницы вне диапазона приводится к существующей.
     */
    @Test
    @DisplayName("Приводит номер страницы к допустимому")
    void clampsPageNumber() {
        Message high = renderer.renderMarket(items(), 99, CURRENCY, MarketRenderer.ALL_FILTER);
        Message low = renderer.renderMarket(items(), -5, CURRENCY, MarketRenderer.ALL_FILTER);

        Assertions.assertTrue(high.text().startsWith("🏪 Рынок (страница 2/2):"));
        Assertions.assertTrue(low.text().startsWith("🏪 Рынок (страница 1/2):"));
    }

    /**
     * Проверяет, что фильтр передаётся в навигационные кнопки.
     */
    @Test
    @DisplayName("Сохраняет фильтр в навигации")
    void keepsFilterInNavigation() {
        Message message = renderer.renderMarket(items(), 1, CURRENCY, "RARE");

        List<Button> navigation = message.buttons().getLast();
        Assertions.assertEquals("/market_page RARE 2", navigation.getFirst().callbackData());
    }

    /**
     * Проверяет наличие кнопок фильтра и кнопки «Мои лоты».
     */
    @Test
    @DisplayName("Добавляет фильтры и кнопку «Мои лоты»")
    void addsFilterAndMineButtons() {
        Message message = renderer.renderMarket(
                List.of(item(1L, HeroRarity.COMMON, 100)), 1, CURRENCY, MarketRenderer.ALL_FILTER);

        List<Button> filterRow = message.buttons().get(1);
        Assertions.assertEquals(5, filterRow.size());
        Assertions.assertEquals("/market_filter COMMON", filterRow.getFirst().callbackData());
        Assertions.assertEquals("/market_filter ALL", filterRow.getLast().callbackData());
        List<Button> mineRow = message.buttons().get(2);
        Assertions.assertEquals("/market_mine", mineRow.getFirst().callbackData());
    }

    /**
     * Проверяет сообщение об отсутствии собственных лотов.
     */
    @Test
    @DisplayName("Сообщает об отсутствии собственных лотов")
    void rendersEmptyUserListings() {
        Message message = renderer.renderUserListings(List.of(), CURRENCY);

        Assertions.assertEquals("📋 У тебя нет активных лотов.", message.text());
        Assertions.assertEquals("/market", message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет отображение собственных лотов.
     */
    @Test
    @DisplayName("Отображает собственные лоты с кнопкой снятия")
    void rendersUserListings() {
        Message message = renderer.renderUserListings(
                List.of(item(3L, HeroRarity.RARE, 200), item(4L, HeroRarity.EPIC, 500)), CURRENCY);

        Assertions.assertEquals("""
                📋 Твои лоты:

                #3 🔵 Hero3 [Редкая] — 200 💎

                #4 🟣 Hero4 [Эпическая] — 500 💎""", message.text());
        Assertions.assertEquals("/market_cancel 3", message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/market_cancel 4", message.buttons().get(1).getFirst().callbackData());
        Assertions.assertEquals("/market", message.buttons().getLast().getFirst().callbackData());
    }

    /**
     * Проверяет сообщение с подтверждением покупки.
     */
    @Test
    @DisplayName("Формирует подтверждение покупки")
    void rendersBuyConfirmation() {
        Message message = renderer.renderBuyConfirmation(item(9L, HeroRarity.LEGENDARY, 1500), CURRENCY);

        Assertions.assertEquals("Купить 🟡 Hero9 [Легендарная] за 1500 💎?", message.text());
        Assertions.assertEquals("/market_buy_confirm 9",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/cancel", message.buttons().getFirst().get(1).callbackData());
    }

    /**
     * Проверяет соответствие эмодзи редкости.
     */
    @Test
    @DisplayName("Возвращает эмодзи редкости")
    void returnsRarityEmoji() {
        Assertions.assertEquals("⚪", renderer.rarityEmoji(HeroRarity.COMMON));
        Assertions.assertEquals("🟡", renderer.rarityEmoji(HeroRarity.LEGENDARY));
    }

    /**
     * Проверяет сообщение об успешной покупке с данными героя.
     */
    @Test
    @DisplayName("Формирует сообщение об успешной покупке")
    void rendersPurchaseSuccess() {
        PurchaseResult result = new PurchaseResult(1L, 2L, 3L, 500);

        Message message = renderer.renderPurchaseSuccess(result, HERO, 250, CURRENCY);

        Assertions.assertEquals("""
                ✅ Покупка совершена!

                🟣 Hero [Эпическая]
                ⚔️ ATK: 10 | 🛡 DEF: 5 | ❤️ HP: 20

                Списано: 500 💎. Твой баланс: 250 💎.""", message.text());
    }

    /**
     * Проверяет сообщение об успешной покупке без данных героя.
     */
    @Test
    @DisplayName("Формирует сообщение об успешной покупке без героя")
    void rendersPurchaseSuccessWithoutHero() {
        PurchaseResult result = new PurchaseResult(1L, 2L, 3L, 500);

        Message message = renderer.renderPurchaseSuccess(result, null, 250, CURRENCY);

        Assertions.assertEquals(String.join(System.lineSeparator(),
                "✅ Покупка совершена!",
                "",
                "Списано: 500 💎. Твой баланс: 250 💎."), message.text());
    }

    /**
     * Проверяет сообщение о снятии лота с продажи.
     */
    @Test
    @DisplayName("Формирует сообщение о снятии лота")
    void rendersListingCancelled() {
        MarketListing listing = new MarketListing(7L, 2L, HERO.id(), 500, CREATED_AT);

        Message message = renderer.renderListingCancelled(listing, HERO);

        Assertions.assertEquals(
                "↩️ Лот #7 снят с продажи. 🟣 Hero [Эпическая] возвращён в твою коллекцию.",
                message.text());
    }

    /**
     * Проверяет сообщение о снятии лота без данных героя.
     */
    @Test
    @DisplayName("Формирует сообщение о снятии лота без героя")
    void rendersListingCancelledWithoutHero() {
        MarketListing listing = new MarketListing(7L, 2L, HERO.id(), 500, CREATED_AT);

        Message message = renderer.renderListingCancelled(listing, null);

        Assertions.assertEquals("↩️ Лот #7 снят с продажи.", message.text());
    }

    /**
     * Проверяет уведомление продавцу.
     */
    @Test
    @DisplayName("Формирует уведомление продавцу")
    void rendersSellerNotification() {
        PurchaseResult result = new PurchaseResult(1L, 2L, HERO.id(), 500);

        Message message = renderer.renderSellerNotification(7L, result, HERO, 475, "Buyer", CURRENCY);

        Assertions.assertEquals(
                "💰 Твой лот #7 (🟣 Hero [Эпическая]) куплен игроком Buyer. Тебе начислено 475 💎.",
                message.text());
    }

    /**
     * Проверяет уведомление продавцу без данных героя.
     */
    @Test
    @DisplayName("Формирует уведомление продавцу без героя")
    void rendersSellerNotificationWithoutHero() {
        PurchaseResult result = new PurchaseResult(1L, 2L, 3L, 500);

        Message message = renderer.renderSellerNotification(7L, result, null, 475, "Buyer", CURRENCY);

        Assertions.assertEquals(
                "💰 Твой лот #7 (#3) куплен игроком Buyer. Тебе начислено 475 💎.",
                message.text());
    }

    /**
     * Проверяет сообщения об ошибках рынка.
     */
    @Test
    @DisplayName("Формирует сообщения об ошибках рынка")
    void rendersErrorMessages() {
        Assertions.assertEquals("❌ Нельзя купить собственный лот.", renderer.renderOwnListing().text());
        Assertions.assertEquals("❌ Этот лот уже продан или снят с продажи.",
                renderer.renderListingUnavailable().text());
        Assertions.assertEquals("❌ Недостаточно средств. Нужно: 500 💎, у тебя: 100 💎.",
                renderer.renderInsufficientFunds(500, 100, CURRENCY).text());
        Assertions.assertEquals("Некорректный номер страницы.", renderer.renderInvalidPage().text());
        Assertions.assertEquals("Некорректный идентификатор лота.", renderer.renderInvalidListingId().text());
    }
}
