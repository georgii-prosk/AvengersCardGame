package com.avengers.cardgame.service.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Тесты для {@link CommandArguments}.
 */
class CommandArgumentsTest {

    /**
     * Проверяет разбиение аргумента на части.
     */
    @Test
    @DisplayName("Разбивает аргумент на части")
    void splitsArgumentIntoParts() {
        Assertions.assertEquals(List.of("1", "5"), CommandArguments.splitParts("1 5"));
    }

    /**
     * Проверяет, что лишние пробелы между частями не создают пустых частей.
     */
    @Test
    @DisplayName("Игнорирует лишние пробелы")
    void ignoresExtraSpaces() {
        Assertions.assertEquals(List.of("1", "5"), CommandArguments.splitParts("1   5"));
    }

    /**
     * Проверяет разбор идентификатора.
     */
    @Test
    @DisplayName("Разбирает идентификатор")
    void parsesId() {
        Assertions.assertEquals(Optional.of(7L), CommandArguments.parseId("7"));
    }

    /**
     * Проверяет, что некорректный идентификатор не разбирается.
     */
    @Test
    @DisplayName("Не разбирает некорректный идентификатор")
    void rejectsInvalidId() {
        Assertions.assertTrue(CommandArguments.parseId("abc").isEmpty());
    }

    /**
     * Проверяет разбор числа.
     */
    @Test
    @DisplayName("Разбирает число")
    void parsesNumber() {
        Assertions.assertEquals(Optional.of(5), CommandArguments.parseNumber("5"));
    }

    /**
     * Проверяет, что некорректное число не разбирается.
     */
    @Test
    @DisplayName("Не разбирает некорректное число")
    void rejectsInvalidNumber() {
        Assertions.assertTrue(CommandArguments.parseNumber("all").isEmpty());
    }

    /**
     * Проверяет разбор пары «идентификатор + число».
     */
    @Test
    @DisplayName("Разбирает пару идентификатор и число")
    void parsesIdAndNumber() {
        Assertions.assertEquals(Optional.of(new CommandArguments.IdAndNumber(1L, 5)),
                CommandArguments.parseIdAndNumber("1 5"));
    }

    /**
     * Проверяет, что пара с лишними частями не разбирается.
     */
    @Test
    @DisplayName("Не разбирает пару с лишними частями")
    void rejectsPairWithExtraParts() {
        Assertions.assertTrue(CommandArguments.parseIdAndNumber("1 5 7").isEmpty());
    }

    /**
     * Проверяет, что пара с некорректным числом не разбирается.
     */
    @Test
    @DisplayName("Не разбирает пару с некорректным числом")
    void rejectsPairWithInvalidNumber() {
        Assertions.assertTrue(CommandArguments.parseIdAndNumber("1 abc").isEmpty());
    }
}
