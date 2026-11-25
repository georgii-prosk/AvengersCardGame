package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;

import java.util.List;

/**
 * Обработчик команды /market_mine (лоты текущего игрока).
 */
public class MarketMineCommandHandler implements CommandHandler {

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
    public MarketMineCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        List<MarketItem> items = marketService.getUserListings(context.userId());
        context.bot().sendMessage(
                marketRenderer.renderUserListings(items, gameConfig.currencyEmoji()), context.chatId());
    }
}
