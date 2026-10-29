package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.impl.InMemoryCommandRegistry;
import com.avengers.cardgame.service.ResolvedCommand;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class CommandRegistryFactoryTest {

    private static final long USER_ID = 11L;
    private static final long CHAT_ID = 110L;

    @Test
    @DisplayName("/daily зарегистрирована и регистрирует пользователя перед выполнением")
    void dailyIsRegisteredAndRegistersUserBeforeRunning() throws Exception {
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        Hero hero = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();

        CommandRegistry registry = new CommandRegistryFactory(
                new UserServiceImpl(userRepository),
                new FakeCardService(List.of(hero)),
                cardRepository,
                heroRepository,
                new InventoryRenderer(cardRepository, heroRepository, new HeroRenderer()),
                new HeroRenderer()
        ).create();
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

        CommandRegistry registry = new CommandRegistryFactory(
                new UserServiceImpl(userRepository),
                new FakeCardService(List.of()),
                cardRepository,
                heroRepository,
                new InventoryRenderer(cardRepository, heroRepository, new HeroRenderer()),
                new HeroRenderer()
        ).create();
        FakeBot bot = new FakeBot();

        Optional<ResolvedCommand> resolved = registry.resolve("/inventory");
        Assertions.assertTrue(resolved.isPresent());
        resolved.get().command().handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertTrue(userRepository.findByUserId(USER_ID).isPresent());
        Assertions.assertEquals(1, bot.count());
    }
}
