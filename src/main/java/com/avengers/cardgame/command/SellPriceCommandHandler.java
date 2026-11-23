package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

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
        Optional<CommandArguments.IdAndNumber> parsed = CommandArguments.parseIdAndNumber(context.argument());
        if (parsed.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidPriceArguments(), context.chatId());
            return;
        }
        long heroId = parsed.get().id();
        int price = parsed.get().number();
        Hero hero = heroRepository.findById(heroId).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(sellRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        GameConfig.RarityConfig rarityConfig = gameConfig.rarity(hero.rarity());
        int minPrice = rarityConfig.minPrice();
        int maxPrice = rarityConfig.maxPrice();
        if (price < minPrice || price > maxPrice) {
            context.bot().sendMessage(sellRenderer.renderInvalidPrice(
                    minPrice, maxPrice, gameConfig.currencyEmoji()), context.chatId());
            return;
        }

        String currency = gameConfig.currencyEmoji();
        int commissionPercent = gameConfig.marketCommissionPercent();
        int sellerAmount = price - price * commissionPercent / 100;
        context.bot().sendMessage(sellRenderer.renderConfirmation(hero, price, sellerAmount,
                commissionPercent, currency), context.chatId());
    }
}
