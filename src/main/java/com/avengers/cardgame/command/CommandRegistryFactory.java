package com.avengers.cardgame.command;

import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.impl.InMemoryCommandRegistry;
import com.avengers.cardgame.service.UserService;

/**
 * Фабрика для создания и настройки реестра команд.
 */
public class CommandRegistryFactory {
    private final UserService userService;
    private final CardService cardService;
    private final UserCardRepository userCardRepository;
    private final HeroRepository heroRepository;
    private final InventoryRenderer inventoryRenderer;
    private final HeroRenderer heroRenderer;

    public CommandRegistryFactory(UserService userService, CardService cardService,
                                  UserCardRepository userCardRepository, HeroRepository heroRepository,
                                  InventoryRenderer inventoryRenderer, HeroRenderer heroRenderer) {
        this.userService = userService;
        this.cardService = cardService;
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
        this.inventoryRenderer = inventoryRenderer;
        this.heroRenderer = heroRenderer;
    }

    /**
     * Создаёт и наполняет реестр команд.
     *
     * @return готовый реестр
     */
    public CommandRegistry create() {
        // ИСПРАВЛЕНО: переменная типа интерфейса, создается реализация
        CommandRegistry registry = new InMemoryCommandRegistry();

        registry.register("/start", wrap(new StartCommandHandler()));
        registry.register("/help", new HelpCommandHandler());
        registry.register("/profile", wrap(new ProfileCommandHandler(userService, userCardRepository)));
        registry.register("/setname", wrap(new SetNameCommandHandler(userService)));
        registry.register("/setbio", wrap(new SetBioCommandHandler(userService)));
        registry.register("/daily", wrap(new DailyCommandHandler(cardService, heroRenderer)));
        registry.register("/inventory", wrap(new InventoryCommandHandler(inventoryRenderer)));
        registry.register("/inventory_page", new InventoryPageCommandHandler(inventoryRenderer));
        registry.register("/view_card", new ViewCardCommandHandler(heroRepository, userCardRepository, heroRenderer));
        return registry;
    }

    /**
     * Оборачивает команду в декоратор, который регистрирует пользователя перед выполнением.
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}