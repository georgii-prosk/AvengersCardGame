package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /market_buy (показ подтверждения покупки лота).
 */
public class MarketBuyCommandHandler implements CommandHandler {

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
    public MarketBuyCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Long> listingId = CommandArguments.parseId(context.argument());
        if (listingId.isEmpty()) {
            context.bot().sendMessage(marketRenderer.renderInvalidListingId(), context.chatId());
            return;
        }

        try {
            MarketItem item = marketService.getListing(listingId.get());
            if (item.listing().sellerId() == context.userId()) {
                context.bot().sendMessage(marketRenderer.renderOwnListing(), context.chatId());
                return;
            }
            context.bot().sendMessage(
                    marketRenderer.renderBuyConfirmation(item, gameConfig.currencyEmoji()),
                    context.chatId());
        } catch (ListingUnavailableException exception) {
            context.bot().sendMessage(marketRenderer.renderListingUnavailable(), context.chatId());
        }
    }
}
