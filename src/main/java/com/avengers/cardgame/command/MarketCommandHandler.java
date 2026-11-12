package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.command.CommandArguments;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Обработчик команды /market и шагов работы с рынком.
 */
public class MarketCommandHandler implements CommandHandler {

    /** Логгер. */
    private static final Logger log = LoggerFactory.getLogger(MarketCommandHandler.class);

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Сервис пользователей. */
    private final UserService userService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер рынка. */
    private final MarketRenderer marketRenderer = new MarketRenderer();

    /**
     * @param marketService  сервис рынка
     * @param heroRepository репозиторий героев
     * @param userService    сервис пользователей
     * @param gameConfig     игровая конфигурация
     */
    public MarketCommandHandler(MarketService marketService, HeroRepository heroRepository,
                                UserService userService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.heroRepository = heroRepository;
        this.userService = userService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        renderMarket(context, null, FIRST_PAGE);
    }

    /**
     * Открывает выбранную страницу рынка с сохранением фильтра.
     *
     * @param context контекст команды
     */
    public void handlePage(CommandContext context) {
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
            context.bot().sendMessage(marketRenderer.renderInvalidFilter(), context.chatId());
            return;
        }
        renderMarket(context, rarity, page);
    }

    /**
     * Применяет фильтр по редкости.
     *
     * @param context контекст команды
     */
    public void handleFilter(CommandContext context) {
        HeroRarity rarity;
        try {
            rarity = parseFilter(context.argument());
        } catch (IllegalArgumentException exception) {
            context.bot().sendMessage(marketRenderer.renderInvalidFilter(), context.chatId());
            return;
        }
        renderMarket(context, rarity, FIRST_PAGE);
    }

    /**
     * Показывает подтверждение покупки лота.
     *
     * @param context контекст команды
     */
    public void handleBuy(CommandContext context) {
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

    /**
     * Выполняет покупку лота и уведомляет продавца.
     *
     * @param context контекст команды
     * @throws AppException при ошибке покупки
     */
    public void handleBuyConfirm(CommandContext context) throws AppException {
        Optional<Long> parsedListingId = CommandArguments.parseId(context.argument());
        if (parsedListingId.isEmpty()) {
            context.bot().sendMessage(marketRenderer.renderInvalidListingId(), context.chatId());
            return;
        }
        long listingId = parsedListingId.get();

        try {
            PurchaseResult result = marketService.buy(context.userId(), listingId);
            Hero hero = heroRepository.findById(result.heroId()).orElse(null);
            User buyer = userService.getByUserId(result.buyerId());

            context.bot().sendMessage(marketRenderer.renderPurchaseSuccess(
                    result, hero, buyer.balance(), gameConfig.currencyEmoji()), context.chatId());

            notifySeller(context, listingId, result, hero, buyer);
        } catch (ListingUnavailableException exception) {
            context.bot().sendMessage(marketRenderer.renderListingUnavailable(), context.chatId());
        } catch (InsufficientFundsException exception) {
            context.bot().sendMessage(marketRenderer.renderInsufficientFunds(
                    exception.required(), exception.available(), gameConfig.currencyEmoji()), context.chatId());
        } catch (OwnListingException exception) {
            context.bot().sendMessage(marketRenderer.renderOwnListing(), context.chatId());
        }
    }

    /**
     * Показывает лоты текущего игрока.
     *
     * @param context контекст команды
     */
    public void handleMine(CommandContext context) {
        List<MarketItem> items = marketService.getUserListings(context.userId());
        context.bot().sendMessage(
                marketRenderer.renderUserListings(items, gameConfig.currencyEmoji()), context.chatId());
    }

    /**
     * Снимает лот с продажи.
     *
     * @param context контекст команды
     * @throws AppException при ошибке снятия лота
     */
    public void handleCancel(CommandContext context) throws AppException {
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

    /**
     * Отправляет продавцу уведомление о покупке.
     * <p>
     * Ошибка отправки логируется и не прерывает основной сценарий.
     *
     * @param context   контекст команды
     * @param listingId идентификатор лота
     * @param result    результат покупки
     * @param hero      приобретённый герой, может быть {@code null}
     * @param buyer     покупатель
     */
    private void notifySeller(CommandContext context, long listingId, PurchaseResult result,
                              @Nullable Hero hero, User buyer) {
        int sellerAmount = result.price() - result.price() * MarketService.COMMISSION_PERCENT / 100;
        Message notification = marketRenderer.renderSellerNotification(listingId, result, hero, sellerAmount,
                buyer.playerName().value(), gameConfig.currencyEmoji());
        try {
            context.bot().sendMessage(notification, result.sellerId());
        } catch (RuntimeException exception) {
            log.error("Failed to notify seller {}", result.sellerId(), exception);
        }
    }

    /**
     * Отправляет страницу рынка.
     *
     * @param context контекст команды
     * @param rarity  фильтр редкости или {@code null}
     * @param page    номер страницы
     */
    private void renderMarket(CommandContext context, @Nullable HeroRarity rarity, int page) {
        String filterCode = rarity == null ? MarketRenderer.ALL_FILTER : rarity.name();
        List<MarketItem> items = marketService.getActiveListings(rarity);
        context.bot().sendMessage(marketRenderer.renderMarket(
                items, page, gameConfig.currencyEmoji(), filterCode), context.chatId());
    }

    /**
     * Разбирает код фильтра.
     *
     * @param filter код фильтра
     * @return редкость или {@code null} для всех редкостей
     * @throws IllegalArgumentException если фильтр неизвестен
     */
    private @Nullable HeroRarity parseFilter(String filter) {
        String normalized = filter == null ? "" : filter.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank() || MarketRenderer.ALL_FILTER.equals(normalized)) {
            return null;
        }
        return HeroRarity.valueOf(normalized);
    }
}
