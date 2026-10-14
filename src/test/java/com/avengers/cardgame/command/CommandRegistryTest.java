package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.service.impl.UserServiceImpl;
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

    /**
     * Создаёт реестр с несколькими командами перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        UserServiceImpl userService = new UserServiceImpl(new FakeUserRepository());

        registry.register("/start", new StartCommand());
        registry.register("/help", new HelpCommand());
        registry.register("/profile", new ProfileCommand(userService));
        registry.register("/setname", new SetNameCommand(userService));
        registry.register("/setbio", new SetBioCommand(userService));
    }

    @Test
    @DisplayName("Разрешает /start без аргумента")
    void resolvesStartWithoutArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/start");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertInstanceOf(StartCommand.class, resolved.get().command());
        Assertions.assertEquals("", resolved.get().argument());
    }

    @Test
    @DisplayName("Разрешает /setname с аргументом")
    void resolvesSetNameWithArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/setname Narek");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertInstanceOf(SetNameCommand.class, resolved.get().command());
        Assertions.assertEquals("Narek", resolved.get().argument());
    }

    @Test
    @DisplayName("Разрешает /setbio с многословным аргументом")
    void resolvesSetBioWithMultiWordArgument() {
        Optional<ResolvedCommand> resolved = registry.resolve("/setbio Привет мир 🦸");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertInstanceOf(SetBioCommand.class, resolved.get().command());
        Assertions.assertEquals("Привет мир 🦸", resolved.get().argument());
    }

    @Test
    @DisplayName("Регистронезависимость имени команды")
    void resolvesCaseInsensitive() {
        Assertions.assertTrue(registry.resolve("/START").isPresent());
        Assertions.assertTrue(registry.resolve("/SetName Narek").isPresent());
    }

    @Test
    @DisplayName("Пустой Optional для неизвестной команды")
    void returnsEmptyForUnknownCommand() {
        Assertions.assertTrue(registry.resolve("/foo").isEmpty());
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
    @DisplayName("Команда без пробела после имени не распознаётся")
    void doesNotMatchCommandWithoutSpace() {
        Assertions.assertTrue(registry.resolve("/setnameX foo").isEmpty());
    }

    @Test
    @DisplayName("Ведущие и хвостовые пробелы не мешают")
    void trimsWhitespace() {
        Optional<ResolvedCommand> resolved = registry.resolve("   /setname Narek   ");

        Assertions.assertTrue(resolved.isPresent());
        Assertions.assertEquals("Narek", resolved.get().argument());
    }
}