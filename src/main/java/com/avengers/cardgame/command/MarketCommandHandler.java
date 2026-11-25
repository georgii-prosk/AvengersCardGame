package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;

import java.util.List;

/**
 * Обработчик команды /market (первая страница рынка).
 */
public class MarketCommandHandler implements CommandHandler {

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
    public MarketCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        List<MarketItem> items = marketService.getActiveListings();
        context.bot().sendMessage(marketRenderer.renderMarket(
                items, FIRST_PAGE, gameConfig.currencyEmoji(), MarketRenderer.ALL_FILTER), context.chatId());
    }
}
