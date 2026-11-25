package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Обработчик команды /market_filter (применение фильтра по редкости).
 */
public class MarketFilterCommandHandler implements CommandHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(MarketFilterCommandHandler.class);

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

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
    public MarketFilterCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        HeroRarity rarity;
        try {
            rarity = parseFilter(context.argument());
        } catch (IllegalArgumentException exception) {
            log.error("Unknown market filter argument: {}", context.argument(), exception);
            return;
        }
        sendPage(context, rarity);
    }

    /**
     * Разбирает код фильтра.
     *
     * @param filter код фильтра
     * @return редкость или {@code null} для всех редкостей
     * @throws IllegalArgumentException если фильтр неизвестен
     */
    @Nullable
    private HeroRarity parseFilter(String filter) {
        String normalized = filter == null ? "" : filter.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank() || MarketRenderer.ALL_FILTER.equals(normalized)) {
            return null;
        }
        return HeroRarity.valueOf(normalized);
    }

    /**
     * Отправляет первую страницу рынка.
     *
     * @param context контекст команды
     * @param rarity  фильтр редкости или {@code null}
     */
    private void sendPage(CommandContext context, @Nullable HeroRarity rarity) {
        String filterCode = rarity == null ? MarketRenderer.ALL_FILTER : rarity.name();
        List<MarketItem> items = marketService.getActiveListings(rarity);
        context.bot().sendMessage(marketRenderer.renderMarket(
                items, FIRST_PAGE, gameConfig.currencyEmoji(), filterCode), context.chatId());
    }
}
