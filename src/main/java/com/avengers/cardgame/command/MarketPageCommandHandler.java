package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Обработчик команды /market_page (переключение страницы рынка с фильтром).
 */
public class MarketPageCommandHandler implements CommandHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(MarketPageCommandHandler.class);

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
    public MarketPageCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        List<String> parts = CommandArguments.splitParts(context.argument());
        String filter = MarketRenderer.ALL_FILTER;
        int page = FIRST_PAGE;
        if (parts.size() == 1 && !parts.getFirst().isBlank()) {
            Optional<Integer> parsedPage = CommandArguments.parseNumber(parts.getFirst());
            if (parsedPage.isPresent()) {
                page = parsedPage.get();
            } else {
                filter = parts.getFirst();
            }
        } else if (parts.size() >= 2) {
            filter = parts.getFirst();
            Optional<Integer> parsedPage = CommandArguments.parseNumber(parts.get(1));
            if (parsedPage.isEmpty()) {
                context.bot().sendMessage(marketRenderer.renderInvalidPage(), context.chatId());
                return;
            }
            page = parsedPage.get();
        }

        HeroRarity rarity;
        try {
            rarity = parseFilter(filter);
        } catch (IllegalArgumentException exception) {
            log.error("Unknown market filter argument: {}", context.argument(), exception);
            return;
        }
        sendPage(context, rarity, page);
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
     * Отправляет страницу рынка.
     *
     * @param context контекст команды
     * @param rarity  фильтр редкости или {@code null}
     * @param page    номер страницы
     */
    private void sendPage(CommandContext context, @Nullable HeroRarity rarity, int page) {
        String filterCode = rarity == null ? MarketRenderer.ALL_FILTER : rarity.name();
        List<MarketItem> items = marketService.getActiveListings(rarity);
        context.bot().sendMessage(marketRenderer.renderMarket(
                items, page, gameConfig.currencyEmoji(), filterCode), context.chatId());
    }
}
