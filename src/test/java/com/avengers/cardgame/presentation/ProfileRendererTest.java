package com.avengers.cardgame.presentation;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link ProfileRenderer}.
 */
class ProfileRendererTest {

    /** Эмодзи валюты. */
    private static final String CURRENCY = "💎";

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Рендерер профиля. */
    private final ProfileRenderer renderer = new ProfileRenderer();

    /**
     * Создаёт профиль игрока.
     *
     * @param bio описание игрока, может быть {@code null}
     * @return профиль игрока
     * @throws AppException если данные профиля некорректны
     */
    private User user(Bio bio) throws AppException {
        return User.of(1L, PlayerName.of("Tony"), bio, 100, 7, 5,
                LocalDateTime.now(), LocalDateTime.now());
    }

    /**
     * Проверяет сообщение с полными данными профиля.
     */
    @Test
    @DisplayName("Формирует сообщение с полными данными профиля")
    void rendersFullProfile() throws AppException {
        Message message = renderer.render(user(new Bio("Genius")), CURRENCY, SHARDS_EMOJI);

        String expected = """
                Имя: Tony
                Описание: Genius
                Твой баланс: 100 💎.
                Кусочки: 7 🧩.
                В твоей коллекции: 5 карточек.""";
        Assertions.assertEquals(expected, message.text());
    }

    /**
     * Проверяет сообщение для пустого описания.
     */
    @Test
    @DisplayName("Подставляет «не задано» вместо пустого описания")
    void rendersNotSetForEmptyBio() throws AppException {
        Message message = renderer.render(user(null), CURRENCY, SHARDS_EMOJI);

        Assertions.assertTrue(message.text().contains("Описание: не задано"));
    }

    /**
     * Проверяет, что эмодзи валюты и кусочков берутся из параметров.
     */
    @Test
    @DisplayName("Использует переданные эмодзи валюты и кусочков")
    void usesGivenEmojis() throws AppException {
        Message message = renderer.render(user(null), "🪙", "⭐");

        Assertions.assertTrue(message.text().contains("Твой баланс: 100 🪙."));
        Assertions.assertTrue(message.text().contains("Кусочки: 7 ⭐."));
    }
}
