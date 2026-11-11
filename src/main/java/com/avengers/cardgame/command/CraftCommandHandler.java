package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.presentation.CraftRenderer;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CraftResult;
import com.avengers.cardgame.service.UserService;

import java.util.Locale;

/**
 * Обработчик команды /craft и шага крафта.
 */
public class CraftCommandHandler implements CommandHandler {

    /** Сервис карточек. */
    private final CardService cardService;

    /** Сервис пользователей. */
    private final UserService userService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер крафта. */
    private final CraftRenderer craftRenderer = new CraftRenderer();

    /**
     * @param cardService сервис карточек
     * @param userService сервис пользователей
     * @param gameConfig  игровая конфигурация
     */
    public CraftCommandHandler(CardService cardService, UserService userService, GameConfig gameConfig) {
        this.cardService = cardService;
        this.userService = userService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        User user = userService.getByUserId(context.userId());
        context.bot().sendMessage(
                craftRenderer.renderMenu(user.shards(), gameConfig.shardsEmoji()), context.chatId());
    }

    /**
     * Выполняет крафт карточки выбранной редкости.
     *
     * @param context контекст команды
     * @throws AppException при ошибке крафта
     */
    public void handleMake(CommandContext context) throws AppException {
        HeroRarity rarity;
        try {
            rarity = HeroRarity.valueOf(context.argument().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            context.bot().sendMessage(craftRenderer.renderUnknownRarity(), context.chatId());
            return;
        }

        try {
            CraftResult result = cardService.craft(context.userId(), rarity);
            context.bot().sendMessage(craftRenderer.renderCraftSuccess(result), context.chatId());
        } catch (InsufficientShardsException exception) {
            context.bot().sendMessage(craftRenderer.renderInsufficientShards(
                    exception.required(), exception.available(), exception.missing()), context.chatId());
        }
    }
}
