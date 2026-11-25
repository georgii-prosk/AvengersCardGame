package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.ResolvedCommand;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

/**
 * Тесты для {@link CommandRegistryFactory}.
 */
class CommandRegistryFactoryTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 11L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 110L;

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new TestGameConfig().defaultConfig();

    /**
     * Создаёт реестр команд.
     *
     * @param userRepository  репозиторий пользователей
     * @param cardRepository  репозиторий карточек
     * @param heroRepository  репозиторий героев
     * @param cardService     сервис карточек
     * @return реестр команд
     */
    private CommandRegistry registryWith(InMemoryUserRepository userRepository,
                                         InMemoryUserCardRepository cardRepository,
                                         InMemoryHeroRepository heroRepository,
                                         FakeCardService cardService) {
        return new CommandRegistryFactory(
                new UserServiceImpl(userRepository),
                cardService,
                new FakeMarketService(),
                cardRepository,
                heroRepository,
                GAME_CONFIG
        ).create();
    }

    @Test
    @DisplayName("/daily зарегистрирована и регистрирует пользователя перед выполнением")
    void dailyIsRegisteredAndRegistersUserBeforeRunning() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        Hero hero = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository(userRepository);
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();

        CommandRegistry registry = registryWith(userRepository, cardRepository, heroRepository,
                new FakeCardService(List.of(hero)));
        FakeBot bot = new FakeBot();

        Optional<ResolvedCommand> resolved = registry.resolve("/daily");
        Assertions.assertTrue(resolved.isPresent());
        resolved.get().command().handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals(2, bot.count());
    }

    @Test
    @DisplayName("/inventory зарегистрирована и регистрирует пользователя перед выполнением")
    void inventoryIsRegisteredAndRegistersUserBeforeRunning() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository(userRepository);
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();

        CommandRegistry registry = registryWith(userRepository, cardRepository, heroRepository,
                new FakeCardService(List.of()));
        FakeBot bot = new FakeBot();

        Optional<ResolvedCommand> resolved = registry.resolve("/inventory");
        Assertions.assertTrue(resolved.isPresent());
        resolved.get().command().handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals(1, bot.count());
    }

    /**
     * Создаёт реестр с пустыми репозиториями.
     *
     * @return реестр команд
     */
    private CommandRegistry emptyRegistry() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        return registryWith(userRepository, new InMemoryUserCardRepository(userRepository),
                new InMemoryHeroRepository(), new FakeCardService(List.of()));
    }

    @Test
    @DisplayName("/disassemble зарегистрирована")
    void disassembleIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/disassemble").isPresent());
    }

    @Test
    @DisplayName("/craft зарегистрирована")
    void craftIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/craft").isPresent());
    }

    @Test
    @DisplayName("/sell зарегистрирована")
    void sellIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/sell").isPresent());
    }

    @Test
    @DisplayName("/market зарегистрирована")
    void marketIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/market").isPresent());
    }

    @Test
    @DisplayName("/cancel зарегистрирована")
    void cancelIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/cancel").isPresent());
    }

    @Test
    @DisplayName("/craft_make зарегистрирована")
    void craftMakeIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/craft_make COMMON").isPresent());
    }

    @Test
    @DisplayName("/market_buy зарегистрирована")
    void marketBuyIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/market_buy 1").isPresent());
    }

    @Test
    @DisplayName("/sell_confirm зарегистрирована")
    void sellConfirmIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/sell_confirm 1 200").isPresent());
    }

    @Test
    @DisplayName("/disassemble_confirm зарегистрирована")
    void disassembleConfirmIsRegistered() {
        Assertions.assertTrue(emptyRegistry().resolve("/disassemble_confirm 1 2").isPresent());
    }
}
