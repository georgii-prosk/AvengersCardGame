package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /sell_price (подтверждение выбранной цены).
 */
public class SellPriceCommandHandler implements CommandHandler {

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param heroRepository репозиторий героев
     * @param gameConfig     игровая конфигурация
     */
    public SellPriceCommandHandler(HeroRepository heroRepository, GameConfig gameConfig) {
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        List<String> parts = CommandArguments.splitParts(context.argument());
        Optional<Long> heroId = parts.size() == 2
                ? CommandArguments.parseId(parts.getFirst())
                : Optional.empty();
        Optional<Integer> price = parts.size() == 2
                ? CommandArguments.parseNumber(parts.getLast())
                : Optional.empty();
        if (heroId.isEmpty() || price.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidPriceArguments(), context.chatId());
            return;
        }
        int priceValue = price.get();
        Hero hero = heroRepository.findById(heroId.get()).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(sellRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        GameConfig.RarityConfig rarityConfig = gameConfig.rarity(hero.rarity());
        int minPrice = rarityConfig.minPrice();
        int maxPrice = rarityConfig.maxPrice();
        if (priceValue < minPrice || priceValue > maxPrice) {
            context.bot().sendMessage(sellRenderer.renderInvalidPrice(
                    minPrice, maxPrice, gameConfig.currencyEmoji()), context.chatId());
            return;
        }

        String currency = gameConfig.currencyEmoji();
        int commissionPercent = gameConfig.marketCommissionPercent();
        int sellerAmount = priceValue - priceValue * commissionPercent / 100;
        context.bot().sendMessage(sellRenderer.renderConfirmation(hero, priceValue, sellerAmount,
                commissionPercent, currency), context.chatId());
    }
}
