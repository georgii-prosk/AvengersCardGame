package com.avengers.cardgame.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * Тесты для {@link CommandRegistry}.
 */
class CommandRegistryTest {

    /** Тестируемый реестр. */
    private CommandRegistry registry;

    /** Первый фейковый обработчик. */
    private CommandHandler firstHandler;

    /** Второй фейковый обработчик. */
    private CommandHandler secondHandler;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        firstHandler = context -> { };
        secondHandler = context -> { };
        registry.register("/first", firstHandler);
        registry.register("/second", secondHandler);
    }

    @Test
    @DisplayName("Разрешает зарегистрированную команду без аргумента")
    void resolvesRegisteredCommandWithoutArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/first");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertSame(firstHandler, resolved.get().command());
        Assertions.assertEquals("", resolved.get().argument());
    }

    @Test
    @DisplayName("Возвращает нужный обработчик для второй команды")
    void resolvesSecondHandler() {
        Optional<ResolvedCommand> resolved = registry.resolve("/second");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertSame(secondHandler, resolved.get().command());
    }

    @Test
    @DisplayName("Разбирает аргумент команды")
    void parsesArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/first hello");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertEquals("hello", resolved.get().argument());
    }

    @Test
    @DisplayName("Разбирает многословный аргумент")
    void parsesMultiWordArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/second hello world 42");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertEquals("hello world 42", resolved.get().argument());
    }

    @Test
    @DisplayName("Регистронезависимость имени команды")
    void resolvesCaseInsensitive() {
        Assertions.assertTrue(registry.resolve("/FIRST").isPresent());
        Assertions.assertTrue(registry.resolve("/First hello").isPresent());
    }

    @Test
    @DisplayName("Пустой Optional для незарегистрированной команды")
    void returnsEmptyForUnknownCommand() {
        Assertions.assertTrue(registry.resolve("/unknown").isEmpty());
    }

    @Test
    @DisplayName("Пустой Optional для null")
    void returnsEmptyForNull() {
        Assertions.assertTrue(registry.resolve(null).isEmpty());
    }

    @Test
    @DisplayName("Пустой Optional для пробелов")
    void returnsEmptyForBlank() {
        Assertions.assertTrue(registry.resolve("   ").isEmpty());
    }

    @Test
    @DisplayName("Префикс без пробела не считается совпадением")
    void doesNotMatchCommandWithoutSpace() {
        Assertions.assertTrue(registry.resolve("/firstX foo").isEmpty());
    }

    @Test
    @DisplayName("Ведущие и хвостовые пробелы не мешают")
    void trimsWhitespace() {
        Optional<ResolvedCommand> resolved = registry.resolve("   /first hello   ");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertEquals("hello", resolved.get().argument());
    }
}