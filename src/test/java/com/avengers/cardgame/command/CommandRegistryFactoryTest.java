package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Тесты для {@link CommandRegistryFactory}.
 */
class CommandRegistryFactoryTest {

    private static final long USER_ID = 11L;
    private static final long CHAT_ID = 110L;

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new GameConfig(
            Duration.ofHours(24), Duration.ofMinutes(30), "Кристаллы", "💎");

    /**
     * Создаёт реестр команд с фейковыми зависимостями.
     *
     * @param userRepository репозиторий пользователей
     * @param cardService    сервис карточек
     * @param cardRepository репозиторий карточек
     * @param heroRepository репозиторий героев
     * @return реестр команд
     */
    private CommandRegistry registry(InMemoryUserRepository userRepository, FakeCardService cardService,
                                     InMemoryUserCardRepository cardRepository,
                                     InMemoryHeroRepository heroRepository) {
        return new CommandRegistryFactory(
                new UserServiceImpl(userRepository),
                cardService,
                new FakeMarketService(),
                cardRepository,
                heroRepository,
                gameConfig
        ).create();
    }

    @Test
    @DisplayName("/daily зарегистрирована и регистрирует пользователя перед выполнением")
    void dailyIsRegisteredAndRegistersUserBeforeRunning() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        Hero hero = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();

        CommandRegistry registry = registry(userRepository, new FakeCardService(List.of(hero)),
                cardRepository, heroRepository);
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
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();

        CommandRegistry registry = registry(userRepository, new FakeCardService(List.of()),
                cardRepository, heroRepository);
        FakeBot bot = new FakeBot();

        Optional<ResolvedCommand> resolved = registry.resolve("/inventory");
        Assertions.assertTrue(resolved.isPresent());
        resolved.get().command().handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Регистрирует команды экономики")
    void registersEconomyCommands() {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        CommandRegistry registry = registry(userRepository, new FakeCardService(List.of()),
                cardRepository, heroRepository);

        List<String> commands = List.of(
                "/disassemble", "/disassemble_pick", "/disassemble_qty", "/disassemble_confirm",
                "/craft", "/craft_make",
                "/sell", "/sell_pick", "/sell_price", "/sell_confirm",
                "/market", "/market_buy", "/market_buy_confirm", "/market_mine", "/market_cancel",
                "/cancel"
        );

        for (String command : commands) {
            Assertions.assertTrue(registry.resolve(command).isPresent(), command);
        }
    }
}
