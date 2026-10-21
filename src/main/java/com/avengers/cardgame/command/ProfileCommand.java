package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /profile.
 */
public class ProfileCommand implements CommandHandler {

    /** Шаблон для вывода профиля. */
    private static final String PROFILE_TEMPLATE = """
            Имя: %s
            Описание: %s
            Твой баланс: %d щитов
            В твоей коллекции: %d карточек
            """;

    /** Заглушка для пустого поля профиля. */
    private static final String NOT_SET = "не задано";

    /** Сервис пользователей. */
    private final UserService userService;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /**
     * @param userService        сервис пользователей
     * @param userCardRepository репозиторий карточек пользователей
     */
    public ProfileCommand(UserService userService, UserCardRepository userCardRepository) {
        this.userService = userService;
        this.userCardRepository = userCardRepository;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        User user = userService.getByUserId(context.userId());

        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();

        int cardCount = userCardRepository.countByUserId(context.userId());

        String text = PROFILE_TEMPLATE.formatted(displayName, bio, user.balance(), cardCount);

        context.bot().sendMessage(new Message(text), context.chatId());
    }
}