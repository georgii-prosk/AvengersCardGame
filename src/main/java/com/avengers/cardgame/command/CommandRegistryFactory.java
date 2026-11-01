package com.avengers.cardgame.command;

import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.impl.CommandRegistryImpl;
import com.avengers.cardgame.service.UserService;

/**
 * Фабрика для создания и настройки реестра команд.
 */
public class CommandRegistryFactory {
    private final UserService userService;
    private final CardService cardService;
    private final UserCardRepository userCardRepository;
    private final HeroRepository heroRepository;

    /**
     * @param userService        сервис пользователей
     * @param cardService        сервис карточек
     * @param userCardRepository репозиторий карточек
     * @param heroRepository     репозиторий героев
     */
    public CommandRegistryFactory(UserService userService, CardService cardService,
                                  UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.userService = userService;
        this.cardService = cardService;
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
    }

    /**
     * Создаёт и наполняет реестр команд.
     *
     * @return готовый реестр
     */
    public CommandRegistry create() {
        CommandRegistry registry = new CommandRegistryImpl();

        registry.register("/start", wrap(new StartCommandHandler()));
        registry.register("/help", new HelpCommandHandler());
        registry.register("/profile", wrap(new ProfileCommandHandler(userService)));
        registry.register("/setname", wrap(new SetNameCommandHandler(userService)));
        registry.register("/setbio", wrap(new SetBioCommandHandler(userService)));
        registry.register("/daily", wrap(new DailyCommandHandler(cardService)));

        registry.register("/inventory", wrap(new InventoryCommandHandler(userCardRepository, heroRepository)));
        registry.register("/inventory_page", new InventoryPageCommandHandler(userCardRepository, heroRepository));

        registry.register("/view_card", new ViewCardCommandHandler(heroRepository, userCardRepository));

        return registry;
    }

    /**
     * Оборачивает команду в декоратор, который регистрирует пользователя перед выполнением.
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}