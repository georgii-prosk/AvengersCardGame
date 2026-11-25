package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.presentation.CraftRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /craft (меню выбора редкости).
 */
public class CraftCommandHandler implements CommandHandler {

    /** Сервис пользователей. */
    private final UserService userService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер крафта. */
    private final CraftRenderer craftRenderer = new CraftRenderer();

    /**
     * @param userService сервис пользователей
     * @param gameConfig  игровая конфигурация
     */
    public CraftCommandHandler(UserService userService, GameConfig gameConfig) {
        this.userService = userService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        User user = userService.getByUserId(context.userId());
        context.bot().sendMessage(
                craftRenderer.renderMenu(user.shards(), gameConfig.shardsEmoji(), gameConfig), context.chatId());
    }
}
