package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.FakeDailyRewardService;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.fakes.FakeHeroRepository;
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

import java.time.Duration;
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
    private static final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /**
     * Создаёт реестр команд.
     *
     * @param userRepository    репозиторий пользователей
     * @param cardRepository   репозиторий карточек
     * @param heroRepository   репозиторий героев
     * @param cardService      сервис карточек
     * @param dailyRewardService сервис ежедневной награды
     * @return реестр команд
     */
    private CommandRegistry registryWith(InMemoryUserRepository userRepository,
                                         InMemoryUserCardRepository cardRepository,
                                         FakeHeroRepository heroRepository,
                                         FakeCardService cardService,
                                         FakeDailyRewardService dailyRewardService) {
        return new CommandRegistryFactory(
                new UserServiceImpl(userRepository),
                cardService,
                dailyRewardService,
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
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        FakeHeroRepository heroRepository = new FakeHeroRepository();

        CommandRegistry registry = registryWith(userRepository, cardRepository, heroRepository,
                new FakeCardService(List.of(hero)), new FakeDailyRewardService(List.of(hero)));
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
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        FakeHeroRepository heroRepository = new FakeHeroRepository();

        CommandRegistry registry = registryWith(userRepository, cardRepository, heroRepository,
                new FakeCardService(List.of()), new FakeDailyRewardService(List.of()));
        FakeBot bot = new FakeBot();

        Optional<ResolvedCommand> resolved = registry.resolve("/inventory");
        Assertions.assertTrue(resolved.isPresent());
        resolved.get().command().handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Новые команды экономики зарегистрированы")
    void economyCommandsAreRegistered() {
        CommandRegistry registry = registryWith(new InMemoryUserRepository(),
                new InMemoryUserCardRepository(), new FakeHeroRepository(),
                new FakeCardService(List.of()), new FakeDailyRewardService(List.of()));

        Assertions.assertTrue(registry.resolve("/disassemble").isPresent());
        Assertions.assertTrue(registry.resolve("/craft").isPresent());
        Assertions.assertTrue(registry.resolve("/sell").isPresent());
        Assertions.assertTrue(registry.resolve("/market").isPresent());
        Assertions.assertTrue(registry.resolve("/cancel").isPresent());
        Assertions.assertTrue(registry.resolve("/craft_make COMMON").isPresent());
        Assertions.assertTrue(registry.resolve("/market_buy 1").isPresent());
        Assertions.assertTrue(registry.resolve("/sell_confirm 1 200").isPresent());
        Assertions.assertTrue(registry.resolve("/disassemble_confirm 1 2").isPresent());
    }
}
