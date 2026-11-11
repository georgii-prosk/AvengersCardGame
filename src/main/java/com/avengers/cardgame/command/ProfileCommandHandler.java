package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.UserService;

/**
 * Обработчик команды /profile.
 */
public class ProfileCommandHandler implements CommandHandler {

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Шаблон для вывода профиля. */
    private static final String PROFILE_TEMPLATE = """
            Имя: %s
            Описание: %s
            Твой баланс: %d %s
            Кусочки: %d %s
            В твоей коллекции: %d карточек
            """;

    /** Заглушка для пустого поля профиля. */
    private static final String NOT_SET = "не задано";

    /** Сервис пользователей. */
    private final UserService userService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

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

        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();

        String text = PROFILE_TEMPLATE.formatted(displayName, bio, user.balance(),
                gameConfig.currencyEmoji(), user.shards(), SHARDS_EMOJI, user.cardCount());

        context.bot().sendMessage(new Message(text), context.chatId());
    }
}
