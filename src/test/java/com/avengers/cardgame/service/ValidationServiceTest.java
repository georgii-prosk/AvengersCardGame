package com.avengers.cardgame.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Тесты для {@link ValidationService}.
 * <p>
 * Проверяют корректность валидации отображаемого имени и описания профиля:
 * допустимые символы, ограничения по длине, обработку {@code null} и пустых
 * значений.
 */
class ValidationServiceTest {

    /** Тестируемый сервис валидации. */
    private ValidationService validationService;

    /**
     * Создаёт новый экземпляр {@link ValidationService} перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    /**
     * Тесты для метода {@link ValidationService#isValidName(String)}.
     */
    @Nested
    @DisplayName("isValidName")
    class IsValidName {

        /**
         * Проверяет, что корректные имена из латинских букв принимаются.
         *
         * @param name корректное имя
         */
        @ParameterizedTest
        @ValueSource(strings = {"Avenger", "IronMan", "a", "Abcdefghijklmnop"})
        @DisplayName("Возвращает true для корректных имён")
        void shouldReturnTrueForValidNames(String name) {
            boolean result = validationService.isValidName(name);
            Assertions.assertTrue(result);
        }

        /**
         * Проверяет, что некорректные имена отклоняются: {@code null},
         * пустые строки, кириллица, пробелы, дефисы, цифры и спецсимволы.
         *
         * @param name некорректное имя
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "Иван", "Tony Stark", "Iron-Man", "Iron_Man",
                "Iron123", "Iron@Man", "Тони"})
        @DisplayName("Возвращает false для некорректных имён")
        void shouldReturnFalseForInvalidNames(String name) {
            boolean result = validationService.isValidName(name);
            Assertions.assertFalse(result);
        }

        /**
         * Проверяет, что имя длиной 129 символов отклоняется.
         */
        @Test
        @DisplayName("Возвращает false, если имя длиннее 128 символов")
        void shouldReturnFalseWhenNameTooLong() {
            String longName = "a".repeat(129);
            boolean result = validationService.isValidName(longName);
            Assertions.assertFalse(result);
        }

        /**
         * Проверяет, что имя длиной ровно 128 символов принимается
         * (граничное значение).
         */
        @Test
        @DisplayName("Возвращает true, если имя ровно 128 символов")
        void shouldReturnTrueWhenNameHasMaxLength() {
            String maxName = "a".repeat(128);
            boolean result = validationService.isValidName(maxName);
            Assertions.assertTrue(result);
        }
    }

    /**
     * Тесты для метода {@link ValidationService#isValidBio(String)}.
     */
    @Nested
    @DisplayName("isValidBio")
    class IsValidBio {

        /**
         * Проверяет, что корректные описания принимаются: латиница, кириллица,
         * цифры, эмодзи, знаки препинания и разрешённые спецсимволы.
         *
         * @param bio корректное описание
         */
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
        @DisplayName("Возвращает true для корректных описаний")
        void shouldReturnTrueForValidBios(String bio) {
            boolean result = validationService.isValidBio(bio);
            Assertions.assertTrue(result);
        }

        /**
         * Проверяет, что некорректные описания отклоняются: {@code null},
         * пустые строки, пробелы и запрещённые символы.
         *
         * @param bio некорректное описание
         */
        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "Описание с недопустимым символом \u0000"})
        @DisplayName("Возвращает false для некорректных описаний")
        void shouldReturnFalseForInvalidBios(String bio) {
            boolean result = validationService.isValidBio(bio);
            Assertions.assertFalse(result);
        }

        /**
         * Проверяет, что описание длиной 257 символов отклоняется.
         */
        @Test
        @DisplayName("Возвращает false, если описание длиннее 256 символов")
        void shouldReturnFalseWhenBioTooLong() {
            String longBio = "a".repeat(257);
            boolean result = validationService.isValidBio(longBio);
            Assertions.assertFalse(result);
        }

        /**
         * Проверяет, что описание длиной ровно 256 символов принимается
         * (граничное значение).
         */
        @Test
        @DisplayName("Возвращает true, если описание ровно 256 символов")
        void shouldReturnTrueWhenBioHasMaxLength() {
            String maxBio = "a".repeat(256);
            boolean result = validationService.isValidBio(maxBio);
            Assertions.assertTrue(result);
        }
    }
}