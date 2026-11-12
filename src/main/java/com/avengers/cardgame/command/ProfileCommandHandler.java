package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.presentation.ProfileRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /profile.
 */
public class ProfileCommandHandler implements CommandHandler {

    /** Сервис пользователей. */
    private final UserService userService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер профиля. */
    private final ProfileRenderer profileRenderer = new ProfileRenderer();

    /**
     * @param userService сервис пользователей
     * @param gameConfig  игровая конфигурация
     */
    public ProfileCommandHandler(UserService userService, GameConfig gameConfig) {
        this.userService = userService;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        User user = userService.getByUserId(context.userId());
        context.bot().sendMessage(profileRenderer.render(
                user, gameConfig.currencyEmoji(), gameConfig.shardsEmoji()), context.chatId());
    }
}
