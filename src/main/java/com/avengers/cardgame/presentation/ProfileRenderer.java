package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.User;
import org.jetbrains.annotations.NotNull;

/**
 * Формирует сообщение с профилем игрока.
 */
public class ProfileRenderer {

    /** Шаблон для вывода профиля. */
    private static final String PROFILE_TEMPLATE = """
            Имя: %s
            Описание: %s
            Твой баланс: %d %s.
            Кусочки: %d %s.
            В твоей коллекции: %d карточек.""";

    /** Заглушка для пустого поля профиля. */
    private static final String NOT_SET = "не задано";

    /**
     * Формирует сообщение с профилем игрока.
     *
     * @param user          профиль игрока
     * @param currencyEmoji эмодзи игровой валюты
     * @param shardsEmoji   эмодзи игровых кусочков
     * @return сообщение с профилем игрока
     */
    public Message render(@NotNull User user, @NotNull String currencyEmoji, @NotNull String shardsEmoji) {
        String displayName = user.playerName().value();
        String bio = user.bio() == null ? NOT_SET : user.bio().value();
        String text = PROFILE_TEMPLATE.formatted(displayName, bio, user.balance(),
                currencyEmoji, user.shards(), shardsEmoji, user.cardCount());
        return new Message(text);
    }
}
