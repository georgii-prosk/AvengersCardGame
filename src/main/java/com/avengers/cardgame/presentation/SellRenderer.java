package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.MarketListing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Формирует сообщения выставления лота на рынок.
 */
public class SellRenderer {

    /** Заголовок выбора карточки. */
    private static final String HEADER_TEMPLATE = "🏷 Выбери карточку для продажи (страница %d/%d):";

    /** Сообщение при отсутствии карточек. */
    private static final String EMPTY_MESSAGE = "У тебя пока нет карточек для продажи.";

    /** Префикс команды выбора карточки. */
    private static final String CARD_COMMAND_PREFIX = "/sell_pick ";

    /** Префикс команды переключения страницы. */
    private static final String PAGE_COMMAND_PREFIX = "/sell_page ";

    /** Префикс команды выбора цены. */
    private static final String PRICE_COMMAND_PREFIX = "/sell_price ";

    /** Префикс команды подтверждения лота. */
    private static final String CONFIRM_COMMAND_PREFIX = "/sell_confirm ";

    /** Текст кнопки подтверждения. */
    private static final String CONFIRM_BUTTON_TEXT = "Подтвердить";

    /** Текст кнопки отмены. */
    private static final String CANCEL_BUTTON_TEXT = "Отмена";

    /** Команда отмены. */
    private static final String CANCEL_COMMAND = "/cancel";

    /** Рендерер страниц инвентаря. */
    private final InventoryRenderer inventoryRenderer = new InventoryRenderer();

    /** Рендерер героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует страницу коллекции для продажи.
     *
     * @param items отсортированный список записей инвентаря
     * @param page  номер страницы
     * @return сообщение со страницей коллекции
     */
    public Message renderSelection(@NotNull List<InventoryItem> items, int page) {
        return inventoryRenderer.render(items, page, EMPTY_MESSAGE, HEADER_TEMPLATE,
                CARD_COMMAND_PREFIX, PAGE_COMMAND_PREFIX);
    }

    /**
     * Формирует сообщение с вариантами цены для выбранной карточки.
     *
     * @param hero     выбранный герой
     * @param minPrice минимальная допустимая цена
     * @param maxPrice максимальная допустимая цена
     * @param currency эмодзи игровой валюты
     * @return сообщение с вариантами цены
     */
    public Message renderPrice(@NotNull Hero hero, int minPrice, int maxPrice, @NotNull String currency) {
        int averagePrice = (minPrice + maxPrice) / 2;
        String text = """
                %s %s [%s]
                Выбери цену. Допустимо от %d до %d %s.""".formatted(
                heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(),
                minPrice, maxPrice, currency);

        List<Button> priceButtons = List.of(
                new Button("Минимум: %d %s".formatted(minPrice, currency),
                        PRICE_COMMAND_PREFIX + hero.id() + " " + minPrice),
                new Button("Средняя: %d %s".formatted(averagePrice, currency),
                        PRICE_COMMAND_PREFIX + hero.id() + " " + averagePrice),
                new Button("Максимум: %d %s".formatted(maxPrice, currency),
                        PRICE_COMMAND_PREFIX + hero.id() + " " + maxPrice)
        );
        return new Message(text, List.of(
                priceButtons,
                List.of(new Button(CANCEL_BUTTON_TEXT, CANCEL_COMMAND))));
    }

    /**
     * Формирует сообщение с подтверждением выставления лота.
     *
     * @param hero             выбранный герой
     * @param price            выбранная цена
     * @param sellerAmount     сумма, которую получит продавец
     * @param commissionPercent процент комиссии рынка
     * @param currency         эмодзи игровой валюты
     * @return сообщение с подтверждением лота
     */
    public Message renderConfirmation(@NotNull Hero hero, int price, int sellerAmount,
                                      int commissionPercent, @NotNull String currency) {
        String text = """
                🏷 Выставить %s %s [%s] за %d %s?
                Комиссия при продаже: %d%%. Ты получишь %d %s.""".formatted(
                heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(),
                price, currency, commissionPercent, sellerAmount, currency);
        return new Message(text, List.of(
                List.of(new Button(CONFIRM_BUTTON_TEXT,
                                CONFIRM_COMMAND_PREFIX + hero.id() + " " + price),
                        new Button(CANCEL_BUTTON_TEXT, CANCEL_COMMAND))));
    }

    /**
     * Формирует сообщение о созданном лоте.
     *
     * @param listing  созданный лот
     * @param hero     герой лота, может быть {@code null}
     * @param currency эмодзи игровой валюты
     * @return сообщение о созданном лоте
     */
    public Message renderCreated(@NotNull MarketListing listing, @Nullable Hero hero, @NotNull String currency) {
        if (hero == null) {
            return new Message("✅ Лот #%d создан за %d %s."
                    .formatted(listing.id(), listing.price(), currency));
        }
        return new Message("✅ Лот #%d создан: %s %s [%s] за %d %s.".formatted(
                listing.id(), heroRenderer.emoji(hero.rarity()), hero.name(),
                hero.rarity().displayName(), listing.price(), currency));
    }

    /**
     * Формирует сообщение о некорректном номере страницы.
     *
     * @return сообщение о некорректном номере страницы
     */
    public Message renderInvalidPage() {
        return new Message("Некорректный номер страницы.");
    }

    /**
     * Формирует сообщение о некорректном идентификаторе карточки.
     *
     * @return сообщение о некорректном идентификаторе карточки
     */
    public Message renderInvalidHeroId() {
        return new Message("Некорректный идентификатор карточки.");
    }

    /**
     * Формирует сообщение об отсутствующем герое.
     *
     * @return сообщение об отсутствующем герое
     */
    public Message renderUnknownHero() {
        return new Message("Такой карточки не существует.");
    }

    /**
     * Формирует сообщение об отсутствии карточки в коллекции.
     *
     * @return сообщение об отсутствии карточки в коллекции
     */
    public Message renderCardNotInCollection() {
        return new Message("Этой карточки нет в твоей коллекции.");
    }

    /**
     * Формирует сообщение о некорректных параметрах цены.
     *
     * @return сообщение о некорректных параметрах цены
     */
    public Message renderInvalidPriceArguments() {
        return new Message("Некорректные параметры цены.");
    }

    /**
     * Формирует сообщение о некорректных параметрах лота.
     *
     * @return сообщение о некорректных параметрах лота
     */
    public Message renderInvalidListingArguments() {
        return new Message("Некорректные параметры лота.");
    }

    /**
     * Формирует сообщение о цене вне допустимого диапазона.
     *
     * @param minPrice минимальная допустимая цена
     * @param maxPrice максимальная допустимая цена
     * @param currency эмодзи игровой валюты
     * @return сообщение о недопустимой цене
     */
    public Message renderInvalidPrice(int minPrice, int maxPrice, @NotNull String currency) {
        return new Message("❌ Цена должна быть от %d до %d %s.".formatted(minPrice, maxPrice, currency));
    }

    /**
     * Формирует сообщение об отсутствии карточки для продажи.
     *
     * @return сообщение об отсутствии карточки для продажи
     */
    public Message renderMissingCard() {
        return new Message("❌ У тебя нет этой карточки в коллекции.");
    }
}
