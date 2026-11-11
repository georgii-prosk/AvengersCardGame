package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.PurchaseResult;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.impl.MarketServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команд рынка карточек.
 * <p>
 * Команды: /market, /market_buy, /market_buy_confirm, /market_mine, /market_cancel
 * (и служебные /market_page, /market_mine_page).
 */
public class MarketCommandHandler implements CommandHandler {

    /** Логгер. */
    private static final Logger logger = LoggerFactory.getLogger(MarketCommandHandler.class);

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Текст ответа при некорректных аргументах. */
    private static final String INVALID_ARGUMENT_MESSAGE = "Некорректный формат команды.";

    /** Текст ответа, если лот уже недоступен. */
    private static final String LISTING_UNAVAILABLE_MESSAGE = "Этот лот больше недоступен. Обнови рынок: /market";

    /** Причина недоступности лота при попытке купить собственный лот. */
    private static final String OWN_LISTING_REASON = "cannot buy own listing";

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер рынка. */
    private final MarketRenderer marketRenderer = new MarketRenderer();

    /** Рендерер карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /** Шаг обработки команды. */
    private final Step step;

    /**
     * Шаги команд рынка.
     */
    public enum Step {
        /** Показ рынка. */
        START,
        /** Переключение страницы рынка. */
        PAGE,
        /** Подтверждение покупки. */
        BUY,
        /** Покупка лота. */
        BUY_CONFIRM,
        /** Показ собственных лотов. */
        MINE,
        /** Переключение страницы собственных лотов. */
        MINE_PAGE,
        /** Снятие лота. */
        CANCEL
    }

    /**
     * @param marketService  сервис рынка
     * @param heroRepository репозиторий героев
     * @param gameConfig     игровая конфигурация
     * @param step           шаг обработки
     */
    public MarketCommandHandler(MarketService marketService, HeroRepository heroRepository,
                                GameConfig gameConfig, @NotNull Step step) {
        this.marketService = marketService;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
        this.step = step;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        switch (step) {
            case START -> showMarket(context, FIRST_PAGE);
            case PAGE -> handlePage(context);
            case BUY -> handleBuy(context);
            case BUY_CONFIRM -> handleBuyConfirm(context);
            case MINE -> showOwnListings(context, FIRST_PAGE);
            case MINE_PAGE -> handleMinePage(context);
            case CANCEL -> handleCancel(context);
        }
    }

    /**
     * Показывает страницу рынка.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void showMarket(CommandContext context, int page) {
        List<MarketItem> items = marketService.getActiveListings();
        context.bot().sendMessage(marketRenderer.render(items, page, gameConfig.currencyEmoji()),
                context.chatId());
    }

    /**
     * Обрабатывает переключение страницы рынка.
     *
     * @param context контекст команды
     */
    private void handlePage(CommandContext context) {
        try {
            showMarket(context, Integer.parseInt(context.argument()));
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
        }
    }

    /**
     * Показывает подтверждение покупки лота.
     *
     * @param context контекст команды
     */
    private void handleBuy(CommandContext context) {
        long listingId;
        try {
            listingId = Long.parseLong(context.argument().trim());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        Optional<MarketItem> item = marketService.getActiveListings().stream()
                .filter(listing -> listing.listing().id() == listingId)
                .findFirst();
        if (item.isEmpty()) {
            context.bot().sendMessage(new Message(LISTING_UNAVAILABLE_MESSAGE), context.chatId());
            return;
        }

        MarketItem marketItem = item.get();
        String text = """
                🛒 Покупка лота
                %s %s — %s
                Цена: %d %s
                Продавец: %s
                Подтвердить покупку?
                """.formatted(heroRenderer.emoji(marketItem.hero().rarity()), marketItem.hero().name(),
                heroRenderer.title(marketItem.hero().rarity()), marketItem.listing().price(),
                gameConfig.currencyEmoji(), marketItem.sellerName());

        List<List<Button>> rows = List.of(
                List.of(new Button("✅ Купить", "/market_buy_confirm " + listingId)),
                List.of(new Button("❌ Отмена", "/cancel"))
        );
        context.bot().sendMessage(new Message(text, rows), context.chatId());
    }

    /**
     * Покупает лот и уведомляет продавца.
     *
     * @param context контекст команды
     */
    private void handleBuyConfirm(CommandContext context) throws UserNotFoundException {
        long listingId;
        try {
            listingId = Long.parseLong(context.argument().trim());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        try {
            PurchaseResult result = marketService.buy(context.userId(), listingId);
            String heroName = heroRepository.findById(result.heroId())
                    .map(Hero::name)
                    .orElse("карточка");
            context.bot().sendMessage(new Message("""
                    ✅ Ты купил %s за %d %s.
                    Карточка добавлена в коллекцию.
                    """.formatted(heroName, result.price(), gameConfig.currencyEmoji())), context.chatId());
            notifySeller(context, result, heroName);
        } catch (InsufficientFundsException exception) {
            context.bot().sendMessage(new Message("Недостаточно средств: нужно %d %s, у тебя %d %s."
                    .formatted(exception.required(), gameConfig.currencyEmoji(),
                            exception.available(), gameConfig.currencyEmoji())), context.chatId());
        } catch (ListingUnavailableException exception) {
            if (OWN_LISTING_REASON.equals(exception.reason())) {
                context.bot().sendMessage(new Message("Нельзя купить собственный лот."), context.chatId());
            } else {
                context.bot().sendMessage(new Message(LISTING_UNAVAILABLE_MESSAGE), context.chatId());
            }
        }
    }

    /**
     * Отправляет продавцу уведомление о продаже.
     * <p>
     * Ошибка отправки логируется и не прерывает основной процесс покупки.
     *
     * @param context  контекст команды
     * @param result   результат покупки
     * @param heroName имя проданного героя
     */
    private void notifySeller(CommandContext context, PurchaseResult result, String heroName) {
        int sellerAmount = result.price()
                - result.price() * MarketServiceImpl.COMMISSION_PERCENT / 100;
        String text = """
                🎉 Твою карточку %s купили за %d %s.
                Тебе начислено: %d %s (после комиссии %d%%).
                """.formatted(heroName, result.price(), gameConfig.currencyEmoji(),
                sellerAmount, gameConfig.currencyEmoji(), MarketServiceImpl.COMMISSION_PERCENT);
        try {
            context.bot().sendMessage(new Message(text), result.sellerId());
        } catch (Exception exception) {
            logger.error("Failed to notify seller {}", result.sellerId(), exception);
        }
    }

    /**
     * Показывает страницу собственных лотов.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void showOwnListings(CommandContext context, int page) {
        List<MarketItem> items = marketService.getUserListings(context.userId());
        context.bot().sendMessage(marketRenderer.renderOwn(items, page, gameConfig.currencyEmoji()),
                context.chatId());
    }

    /**
     * Обрабатывает переключение страницы собственных лотов.
     *
     * @param context контекст команды
     */
    private void handleMinePage(CommandContext context) {
        try {
            showOwnListings(context, Integer.parseInt(context.argument()));
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
        }
    }

    /**
     * Снимает лот с рынка.
     *
     * @param context контекст команды
     */
    private void handleCancel(CommandContext context) throws UserNotFoundException {
        long listingId;
        try {
            listingId = Long.parseLong(context.argument().trim());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        try {
            marketService.cancelListing(context.userId(), listingId);
            context.bot().sendMessage(new Message("""
                    ✅ Лот снят с рынка.
                    Карточка возвращена в коллекцию.
                    """), context.chatId());
        } catch (ListingUnavailableException exception) {
            context.bot().sendMessage(new Message("Этот лот не найден среди твоих лотов."), context.chatId());
        }
    }
}
