package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidPlayerNameException;
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
        void acceptsValidNames(String name) throws Exception {
            PlayerName playerName = PlayerName.of(name);

            Assertions.assertEquals(name, playerName.value());
        }

        @Test
        @DisplayName("Принимает имя ровно 128 символов")
        void acceptsMaxLengthName() throws Exception {
            String name = "a".repeat(128);

            PlayerName playerName = PlayerName.of(name); // <-- Используем of()

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
            Assertions.assertThrows(InvalidPlayerNameException.class,
                    () -> PlayerName.of(name)); // <-- Используем of()
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "Иван", "Tony Stark", "Iron-Man", "Iron123", "Iron@Man"})
        @DisplayName("Отклоняет недопустимые имена")
        void rejectsInvalidNames(String name) {
            Assertions.assertThrows(InvalidPlayerNameException.class,
                    () -> PlayerName.of(name)); // <-- Используем of()
        }

        @Test
        @DisplayName("Отклоняет имя длиннее 128 символов")
        void rejectsNameTooLong() {
            String name = "a".repeat(129);

            Assertions.assertThrows(InvalidPlayerNameException.class,
                    () -> PlayerName.of(name)); // <-- Используем of()
        }
    }
}