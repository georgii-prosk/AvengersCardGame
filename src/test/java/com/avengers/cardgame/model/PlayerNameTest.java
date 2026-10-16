package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Тесты для {@link PlayerName}.
 */
class PlayerNameTest {

    @Nested
    @DisplayName("Успешное создание")
    class Success {

        @ParameterizedTest
        @ValueSource(strings = {"Avenger", "IronMan", "a", "Abcdefghijklmnop"})
        @DisplayName("Принимает валидные имена")
        void acceptsValidNames(String name) {
            PlayerName playerName = new PlayerName(name);

            Assertions.assertEquals(name, playerName.value());
        }

        @Test
        @DisplayName("Принимает имя ровно 128 символов")
        void acceptsMaxLengthName() {
            String name = "a".repeat(128);

            PlayerName playerName = new PlayerName(name);

            Assertions.assertEquals(name, playerName.value());
        }
    }

    @Nested
    @DisplayName("Ошибки валидации")
    class ValidationErrors {

        @ParameterizedTest
        @NullSource
        @DisplayName("Отклоняет null")
        void rejectsNull(String name) {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> new PlayerName(name));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "Иван", "Tony Stark", "Iron-Man", "Iron123", "Iron@Man"})
        @DisplayName("Отклоняет недопустимые имена")
        void rejectsInvalidNames(String name) {
            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> new PlayerName(name));
        }

        @Test
        @DisplayName("Отклоняет имя длиннее 128 символов")
        void rejectsNameTooLong() {
            String name = "a".repeat(129);

            Assertions.assertThrows(IllegalArgumentException.class,
                    () -> new PlayerName(name));
        }
    }
}