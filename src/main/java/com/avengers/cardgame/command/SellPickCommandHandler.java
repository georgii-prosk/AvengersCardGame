package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /sell_pick (выбор карточки и вариантов цены).
 */
public class SellPickCommandHandler implements CommandHandler {

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public SellPickCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                  GameConfig gameConfig) {
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Long> parsedHeroId = CommandArguments.parseId(context.argument());
        if (parsedHeroId.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidHeroId(), context.chatId());
            return;
        }
        long heroId = parsedHeroId.get();
        Hero hero = heroRepository.findById(heroId).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(sellRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        if (inventoryLoader.countCards(context.userId(), heroId) <= 0) {
            context.bot().sendMessage(sellRenderer.renderCardNotInCollection(), context.chatId());
            return;
        }

        GameConfig.RarityConfig rarityConfig = gameConfig.rarity(hero.rarity());
        context.bot().sendMessage(sellRenderer.renderPrice(hero, rarityConfig.minPrice(), rarityConfig.maxPrice(),
                gameConfig.currencyEmoji()), context.chatId());
    }
}
