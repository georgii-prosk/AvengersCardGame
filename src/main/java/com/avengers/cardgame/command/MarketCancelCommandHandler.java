package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /market_cancel (снятие лота с продажи).
 */
public class MarketCancelCommandHandler implements CommandHandler {

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Рендерер рынка. */
    private final MarketRenderer marketRenderer = new MarketRenderer();

    /**
     * @param marketService  сервис рынка
     * @param heroRepository репозиторий героев
     */
    public MarketCancelCommandHandler(MarketService marketService, HeroRepository heroRepository) {
        this.marketService = marketService;
        this.heroRepository = heroRepository;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        Optional<Long> listingId = CommandArguments.parseId(context.argument());
        if (listingId.isEmpty()) {
            context.bot().sendMessage(marketRenderer.renderInvalidListingId(), context.chatId());
            return;
        }

        try {
            MarketListing listing = marketService.cancelListing(context.userId(), listingId.get());
            Hero hero = heroRepository.findById(listing.heroId()).orElse(null);
            context.bot().sendMessage(marketRenderer.renderListingCancelled(listing, hero), context.chatId());
        } catch (ListingUnavailableException exception) {
            context.bot().sendMessage(marketRenderer.renderListingUnavailable(), context.chatId());
        }
    }
}
