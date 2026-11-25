package com.avengers.cardgame.presentation;

import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.command.CommandNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Формирует текстовое представление рынка с пагинацией.
 */
public class MarketRenderer {

    /** Количество лотов, отображаемых на одной странице. */
    private static final int PAGE_SIZE = 5;

    /** Код фильтра «все редкости». */
    public static final String ALL_FILTER = "ALL";

    /** Сообщение при отсутствии лотов на рынке. */
    private static final String EMPTY_MARKET_MESSAGE =
            "Сейчас на рынке нет лотов. Будь первым — выставь карточку через /sell!";

    /** Сообщение при отсутствии собственных лотов. */
    private static final String EMPTY_USER_LISTINGS_MESSAGE = "📋 У тебя нет активных лотов.";

    /** Шаблон заголовка рынка. */
    private static final String MARKET_HEADER_TEMPLATE = "🏪 Рынок (страница %d/%d):";

    /** Шаблон строки лота. */
    private static final String LISTING_TEMPLATE = "#%d %s %s [%s] — %d %s (продавец: %s)";

    /** Шаблон строки собственного лота. */
    private static final String USER_LISTING_TEMPLATE = "#%d %s %s [%s] — %d %s";

    /** Шаблон надписи кнопки покупки. */
    private static final String BUY_BUTTON_TEMPLATE = "Купить #%d";

    /** Шаблон надписи кнопки снятия лота. */
    private static final String CANCEL_BUTTON_TEMPLATE = "Снять #%d";

    /** Надпись кнопки перехода к рынку. */
    private static final String BACK_TO_MARKET_BUTTON = "◀️ К рынку";

    /** Надпись кнопки перехода на предыдущую страницу. */
    private static final String PREVIOUS_BUTTON_TEXT = "◀️ Назад";

    /** Надпись кнопки перехода на следующую страницу. */
    private static final String NEXT_BUTTON_TEXT = "Вперёд ▶️";

    /** Надпись кнопки «Мои лоты». */
    private static final String MY_LISTINGS_BUTTON = "Мои лоты";

    /** Надпись кнопки фильтра «Все». */
    private static final String ALL_FILTER_BUTTON = "Все";

    /** Сообщение о недоступном лоте. */
    private static final String LISTING_UNAVAILABLE_MESSAGE = "❌ Этот лот уже продан или снят с продажи.";

    /** Сообщение о попытке купить свой лот. */
    private static final String OWN_LISTING_MESSAGE = "❌ Нельзя купить собственный лот.";

    /** Сообщение о некорректном номере страницы. */
    private static final String INVALID_PAGE_MESSAGE = "Некорректный номер страницы.";

    /** Сообщение о некорректном идентификаторе лота. */
    private static final String INVALID_LISTING_ID_MESSAGE = "Некорректный идентификатор лота.";

    /** Рендерер для карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует сообщение со страницей рынка.
     *
     * @param items         отсортированный список объявлений
     * @param page          запрошенная страница (начиная с 1)
     * @param currencyEmoji эмодзи игровой валюты
     * @param filterCode    код текущего фильтра редкости или {@link #ALL_FILTER}
     * @return сообщение с текстом и кнопками
     */
    public Message renderMarket(@NotNull List<MarketItem> items, int page,
                                @NotNull String currencyEmoji, @NotNull String filterCode) {
        if (items.isEmpty()) {
            return new Message(EMPTY_MARKET_MESSAGE);
        }

        int totalPages = (items.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = Math.clamp(page, 1, totalPages);
        int from = (currentPage - 1) * PAGE_SIZE;
        List<MarketItem> pageItems = items.subList(from, Math.min(from + PAGE_SIZE, items.size()));

        StringBuilder text = new StringBuilder(MARKET_HEADER_TEMPLATE.formatted(currentPage, totalPages));
        List<List<Button>> rows = new ArrayList<>();
        List<Button> buyButtons = new ArrayList<>();

        for (MarketItem item : pageItems) {
            var listing = item.listing();
            var hero = item.hero();
            text.append('\n').append(LISTING_TEMPLATE.formatted(
                    listing.id(), heroRenderer.emoji(hero.rarity()), hero.name(),
                    heroRenderer.title(hero.rarity()), listing.price(), currencyEmoji, item.sellerName()));
            buyButtons.add(new Button(BUY_BUTTON_TEMPLATE.formatted(listing.id()),
                    CommandNames.MARKET_BUY + " " + listing.id()));
        }

        rows.add(buyButtons);
        rows.add(filterButtons());
        rows.add(List.of(new Button(MY_LISTINGS_BUTTON, CommandNames.MARKET_MINE)));

        List<Button> navigation = new ArrayList<>();
        if (currentPage > 1) {
            navigation.add(new Button(PREVIOUS_BUTTON_TEXT,
                    CommandNames.MARKET_PAGE + " " + filterCode + " " + (currentPage - 1)));
        }
        if (currentPage < totalPages) {
            navigation.add(new Button(NEXT_BUTTON_TEXT,
                    CommandNames.MARKET_PAGE + " " + filterCode + " " + (currentPage + 1)));
        }
        if (!navigation.isEmpty()) {
            rows.add(navigation);
        }

        return new Message(text.toString(), rows);
    }

    /**
     * Формирует сообщение со списком собственных лотов игрока.
     *
     * @param items         список объявлений игрока
     * @param currencyEmoji эмодзи игровой валюты
     * @return сообщение с текстом и кнопками
     */
    public Message renderUserListings(@NotNull List<MarketItem> items, @NotNull String currencyEmoji) {
        if (items.isEmpty()) {
            return new Message(EMPTY_USER_LISTINGS_MESSAGE,
                    List.of(List.of(new Button(BACK_TO_MARKET_BUTTON, CommandNames.MARKET))));
        }

        StringBuilder text = new StringBuilder("📋 Твои лоты:");
        List<List<Button>> rows = new ArrayList<>();

        for (MarketItem item : items) {
            var listing = item.listing();
            Hero hero = item.hero();
            text.append("\n\n").append(USER_LISTING_TEMPLATE.formatted(
                    listing.id(), heroRenderer.emoji(hero.rarity()), hero.name(),
                    heroRenderer.title(hero.rarity()), listing.price(), currencyEmoji));
            rows.add(List.of(new Button(CANCEL_BUTTON_TEMPLATE.formatted(listing.id()),
                    CommandNames.MARKET_CANCEL + " " + listing.id())));
        }

        rows.add(List.of(new Button(BACK_TO_MARKET_BUTTON, CommandNames.MARKET)));
        return new Message(text.toString(), rows);
    }

    /**
     * Формирует сообщение с подтверждением покупки лота.
     *
     * @param item          объявление рынка
     * @param currencyEmoji эмодзи игровой валюты
     * @return сообщение с кнопками подтверждения и отмены
     */
    public Message renderBuyConfirmation(@NotNull MarketItem item, @NotNull String currencyEmoji) {
        Hero hero = item.hero();
        String text = "Купить %s %s [%s] за %d %s?".formatted(
                heroRenderer.emoji(hero.rarity()), hero.name(), heroRenderer.title(hero.rarity()),
                item.listing().price(), currencyEmoji);
        return new Message(text, List.of(
                List.of(new Button("Подтвердить", CommandNames.MARKET_BUY_CONFIRM + " " + item.listing().id()),
                        new Button("Отмена", CommandNames.CANCEL))));
    }

    /**
     * Формирует сообщение об успешной покупке лота.
     *
     * @param result        результат покупки
     * @param hero          приобретённый герой, может быть {@code null}
     * @param buyerBalance  баланс покупателя после покупки
     * @param currencyEmoji эмодзи игровой валюты
     * @return сообщение об успешной покупке
     */
    public Message renderPurchaseSuccess(@NotNull PurchaseResult result, @Nullable Hero hero,
                                         int buyerBalance, @NotNull String currencyEmoji) {
        if (hero == null) {
            return new Message("✅ Покупка совершена!%n%nСписано: %d %s. Твой баланс: %d %s."
                    .formatted(result.price(), currencyEmoji, buyerBalance, currencyEmoji));
        }
        String text = """
                ✅ Покупка совершена!

                %s %s [%s]
                ⚔️ ATK: %d | 🛡 DEF: %d | ❤️ HP: %d

                Списано: %d %s. Твой баланс: %d %s.""".formatted(
                heroRenderer.emoji(hero.rarity()), hero.name(), heroRenderer.title(hero.rarity()),
                hero.attack(), hero.defense(), hero.health(),
                result.price(), currencyEmoji, buyerBalance, currencyEmoji);
        return new Message(text);
    }

    /**
     * Формирует сообщение о снятии лота с продажи.
     *
     * @param listing снятый лот
     * @param hero    герой лота, может быть {@code null}
     * @return сообщение о снятии лота с продажи
     */
    public Message renderListingCancelled(@NotNull MarketListing listing, @Nullable Hero hero) {
        if (hero == null) {
            return new Message("↩️ Лот #%d снят с продажи.".formatted(listing.id()));
        }
        return new Message("↩️ Лот #%d снят с продажи. %s %s [%s] возвращён в твою коллекцию.".formatted(
                listing.id(), heroRenderer.emoji(hero.rarity()), hero.name(), heroRenderer.title(hero.rarity())));
    }

    /**
     * Формирует уведомление продавцу о покупке его лота.
     *
     * @param listingId    идентификатор лота
     * @param result       результат покупки
     * @param hero         приобретённый герой, может быть {@code null}
     * @param sellerAmount сумма, начисленная продавцу
     * @param buyerName    имя покупателя
     * @param currencyEmoji эмодзи игровой валюты
     * @return уведомление продавцу
     */
    public Message renderSellerNotification(long listingId, @NotNull PurchaseResult result, @Nullable Hero hero,
                                            int sellerAmount, @NotNull String buyerName,
                                            @NotNull String currencyEmoji) {
        String heroDescription = hero == null
                ? "#" + result.heroId()
                : "%s %s [%s]".formatted(heroRenderer.emoji(hero.rarity()), hero.name(),
                        heroRenderer.title(hero.rarity()));
        String text = "💰 Твой лот #%d (%s) куплен игроком %s. Тебе начислено %d %s."
                .formatted(listingId, heroDescription, buyerName, sellerAmount, currencyEmoji);
        return new Message(text);
    }

    /**
     * Формирует сообщение о попытке купить собственный лот.
     *
     * @return сообщение о покупке собственного лота
     */
    public Message renderOwnListing() {
        return new Message(OWN_LISTING_MESSAGE);
    }

    /**
     * Формирует сообщение о недоступном лоте.
     *
     * @return сообщение о недоступном лоте
     */
    public Message renderListingUnavailable() {
        return new Message(LISTING_UNAVAILABLE_MESSAGE);
    }

    /**
     * Формирует сообщение о нехватке средств.
     *
     * @param required     требуемая сумма
     * @param available    доступная сумма
     * @param currencyEmoji эмодзи игровой валюты
     * @return сообщение о нехватке средств
     */
    public Message renderInsufficientFunds(int required, int available, @NotNull String currencyEmoji) {
        return new Message("❌ Недостаточно средств. Нужно: %d %s, у тебя: %d %s."
                .formatted(required, currencyEmoji, available, currencyEmoji));
    }

    /**
     * Формирует сообщение о некорректном номере страницы.
     *
     * @return сообщение о некорректном номере страницы
     */
    public Message renderInvalidPage() {
        return new Message(INVALID_PAGE_MESSAGE);
    }

    /**
     * Формирует сообщение о некорректном идентификаторе лота.
     *
     * @return сообщение о некорректном идентификаторе лота
     */
    public Message renderInvalidListingId() {
        return new Message(INVALID_LISTING_ID_MESSAGE);
    }

    /**
     * Возвращает эмодзи, соответствующий редкости.
     *
     * @param rarity редкость
     * @return эмодзи редкости
     */
    public String rarityEmoji(@NotNull HeroRarity rarity) {
        return heroRenderer.emoji(rarity);
    }

    /**
     * Возвращает ряд кнопок фильтра по редкости.
     *
     * @return ряд кнопок
     */
    private List<Button> filterButtons() {
        return List.of(
                new Button(heroRenderer.emoji(HeroRarity.COMMON), CommandNames.MARKET_FILTER + " " + HeroRarity.COMMON.name()),
                new Button(heroRenderer.emoji(HeroRarity.RARE), CommandNames.MARKET_FILTER + " " + HeroRarity.RARE.name()),
                new Button(heroRenderer.emoji(HeroRarity.EPIC), CommandNames.MARKET_FILTER + " " + HeroRarity.EPIC.name()),
                new Button(heroRenderer.emoji(HeroRarity.LEGENDARY), CommandNames.MARKET_FILTER + " " + HeroRarity.LEGENDARY.name()),
                new Button(ALL_FILTER_BUTTON, CommandNames.MARKET_FILTER + " " + ALL_FILTER)
        );
    }
}
