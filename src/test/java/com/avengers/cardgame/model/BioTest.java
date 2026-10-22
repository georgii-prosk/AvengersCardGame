package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidBioException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Тесты для {@link Bio}.
 */
class BioTest {

    @Nested
    @DisplayName("Успешное создание")
    class Success {

        @ParameterizedTest
        @ValueSource(strings = {
                "Просто описание",
                "Simple bio",
                "Bio with emoji 😀🔥",
                "Bio с цифрами 123",
                "Bio with punctuation: hello, world!",
                "Спецсимволы: /|\\<>@#$%^&*(){}`~;:'\"-_=+[].,!?",
                "a"
        })
        @DisplayName("Принимает валидные описания")
        void acceptsValidBios(String bio) throws InvalidBioException {
            Bio value = Bio.of(bio);

            Assertions.assertEquals(bio, value.value());
        }

        @Test
        @DisplayName("Принимает описание ровно 256 символов")
        void acceptsMaxLengthBio() throws InvalidBioException {
            String bio = "a".repeat(256);

            Bio value = Bio.of(bio);

            Assertions.assertEquals(bio, value.value());
        }
    }

    @Nested
    @DisplayName("Ошибки валидации")
    class ValidationErrors {

        @ParameterizedTest
        @NullSource
        @DisplayName("Отклоняет null")
        void rejectsNull(String bio) {
            Assertions.assertThrows(InvalidBioException.class,
                    () -> Bio.of(bio));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   "})
        @DisplayName("Отклоняет пустое описание")
        void rejectsBlankBio(String bio) {
            Assertions.assertThrows(InvalidBioException.class,
                    () -> Bio.of(bio));
        }

        @Test
        @DisplayName("Отклоняет описание длиннее 256 символов")
        void rejectsBioTooLong() {
            String bio = "a".repeat(257);

            Assertions.assertThrows(InvalidBioException.class,
                    () -> Bio.of(bio));
        }
    }
}