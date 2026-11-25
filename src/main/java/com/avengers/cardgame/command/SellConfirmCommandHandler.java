package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /sell_confirm (выставление лота на рынок).
 */
public class SellConfirmCommandHandler implements CommandHandler {

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param marketService  сервис рынка
     * @param heroRepository репозиторий героев
     * @param gameConfig     игровая конфигурация
     */
    public SellConfirmCommandHandler(MarketService marketService, HeroRepository heroRepository,
                                     GameConfig gameConfig) {
        this.marketService = marketService;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        List<String> parts = CommandArguments.splitParts(context.argument());
        Optional<Long> heroId = parts.size() == 2
                ? CommandArguments.parseId(parts.getFirst())
                : Optional.empty();
        Optional<Integer> price = parts.size() == 2
                ? CommandArguments.parseNumber(parts.getLast())
                : Optional.empty();
        if (heroId.isEmpty() || price.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidListingArguments(), context.chatId());
            return;
        }

        try {
            MarketListing listing = marketService.createListing(context.userId(), heroId.get(), price.get());
            Hero hero = heroRepository.findById(listing.heroId()).orElse(null);
            context.bot().sendMessage(sellRenderer.renderCreated(
                    listing, hero, gameConfig.currencyEmoji()), context.chatId());
        } catch (NotEnoughCardsException exception) {
            context.bot().sendMessage(sellRenderer.renderMissingCard(), context.chatId());
        } catch (InvalidPriceException exception) {
            context.bot().sendMessage(sellRenderer.renderInvalidPrice(
                    exception.minPrice(), exception.maxPrice(), gameConfig.currencyEmoji()), context.chatId());
        }
    }
}
