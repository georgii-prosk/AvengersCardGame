package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /sell и шагов выставления лота.
 */
public class SellCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param marketService      сервис рынка
     * @param gameConfig         игровая конфигурация
     */
    public SellCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                              MarketService marketService, GameConfig gameConfig) {
        this.heroRepository = heroRepository;
        this.marketService = marketService;
        this.gameConfig = gameConfig;
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        sendSelection(context, FIRST_PAGE);
    }

    /**
     * Открывает выбранную страницу коллекции для продажи.
     *
     * @param context контекст команды
     */
    public void handlePage(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        sendSelection(context, page.get());
    }

    /**
     * Показывает варианты цены для выбранной карточки.
     *
     * @param context контекст команды
     */
    public void handlePick(CommandContext context) {
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

        int minPrice = hero.rarity().minPrice();
        int maxPrice = hero.rarity().maxPrice();
        context.bot().sendMessage(sellRenderer.renderPrice(hero, minPrice, maxPrice,
                gameConfig.currencyEmoji()), context.chatId());
    }

    /**
     * Показывает подтверждение выставления лота по выбранной цене.
     *
     * @param context контекст команды
     */
    public void handlePrice(CommandContext context) {
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
        int minPrice = hero.rarity().minPrice();
        int maxPrice = hero.rarity().maxPrice();
        if (price < minPrice || price > maxPrice) {
            context.bot().sendMessage(sellRenderer.renderInvalidPrice(
                    minPrice, maxPrice, gameConfig.currencyEmoji()), context.chatId());
            return;
        }

        String currency = gameConfig.currencyEmoji();
        int sellerAmount = price - price * MarketService.COMMISSION_PERCENT / 100;
        context.bot().sendMessage(sellRenderer.renderConfirmation(hero, price, sellerAmount,
                MarketService.COMMISSION_PERCENT, currency), context.chatId());
    }

    /**
     * Выставляет лот на рынок.
     *
     * @param context контекст команды
     * @throws AppException при ошибке выставления лота
     */
    public void handleConfirm(CommandContext context) throws AppException {
        Optional<CommandArguments.IdAndNumber> parsed = CommandArguments.parseIdAndNumber(context.argument());
        if (parsed.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidListingArguments(), context.chatId());
            return;
        }
        long heroId = parsed.get().id();
        int price = parsed.get().number();

        try {
            MarketListing listing = marketService.createListing(context.userId(), heroId, price);
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

    /**
     * Отправляет страницу коллекции для продажи.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void sendSelection(CommandContext context, int page) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(sellRenderer.renderSelection(items, page), context.chatId());
    }
}
