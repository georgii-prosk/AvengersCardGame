package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

/**
 * Общая логика отправки страницы рынка и разбора фильтра редкости.
 */
class MarketPageSender {

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер рынка. */
    private final MarketRenderer marketRenderer = new MarketRenderer();

    /**
     * @param marketService сервис рынка
     * @param gameConfig    игровая конфигурация
     */
    MarketPageSender(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    /**
     * Отправляет страницу рынка.
     *
     * @param context контекст команды
     * @param rarity  фильтр редкости или {@code null}
     * @param page    номер страницы
     */
    void send(CommandContext context, @Nullable HeroRarity rarity, int page) {
        String filterCode = rarity == null ? MarketRenderer.ALL_FILTER : rarity.name();
        List<MarketItem> items = marketService.getActiveListings(rarity);
        context.bot().sendMessage(marketRenderer.renderMarket(
                items, page, gameConfig.currencyEmoji(), filterCode), context.chatId());
    }

    /**
     * Разбирает код фильтра.
     *
     * @param filter код фильтра
     * @return редкость или {@code null} для всех редкостей
     * @throws IllegalArgumentException если фильтр неизвестен
     */
    @Nullable HeroRarity parseFilter(String filter) {
        String normalized = filter == null ? "" : filter.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank() || MarketRenderer.ALL_FILTER.equals(normalized)) {
            return null;
        }
        return HeroRarity.valueOf(normalized);
    }

    /**
     * Отправляет сообщение о некорректном номере страницы.
     *
     * @param context контекст команды
     */
    void sendInvalidPage(CommandContext context) {
        context.bot().sendMessage(marketRenderer.renderInvalidPage(), context.chatId());
    }
}
