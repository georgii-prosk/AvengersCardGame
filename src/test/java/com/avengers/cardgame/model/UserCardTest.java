package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link UserCard}.
 */
class UserCardTest {

    /**
     * Проверяет создание карточки с валидными данными.
     */
    @Test
    @DisplayName("Создаёт карточку с валидными данными")
    void createsValidCard() {
        UserCard card = new UserCard(1L, 2L, 3);

        Assertions.assertEquals(1L, card.userId());
        Assertions.assertEquals(2L, card.heroId());
        Assertions.assertEquals(3, card.count());
    }

    /**
     * Проверяет, что отрицательное количество отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательном количестве")
    void throwsOnNegativeCount() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new UserCard(1L, 2L, -1));
    }
}