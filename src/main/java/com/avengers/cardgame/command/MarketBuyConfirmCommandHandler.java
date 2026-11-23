package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
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

import java.util.Optional;

/**
 * Обработчик команды /market_buy_confirm (выполнение покупки и уведомление продавца).
 */
public class MarketBuyConfirmCommandHandler implements CommandHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(MarketBuyConfirmCommandHandler.class);

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
    public MarketBuyConfirmCommandHandler(MarketService marketService, HeroRepository heroRepository,
                                          UserService userService, GameConfig gameConfig) {
        this.marketService = marketService;
        this.heroRepository = heroRepository;
        this.userService = userService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
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
        int commissionPercent = gameConfig.marketCommissionPercent();
        int sellerAmount = result.price() - result.price() * commissionPercent / 100;
        Message notification = marketRenderer.renderSellerNotification(listingId, result, hero, sellerAmount,
                buyer.playerName().value(), gameConfig.currencyEmoji());
        try {
            context.bot().sendMessage(notification, result.sellerId());
        } catch (RuntimeException exception) {
            log.error("Failed to notify seller {}", result.sellerId(), exception);
        }
    }
}
