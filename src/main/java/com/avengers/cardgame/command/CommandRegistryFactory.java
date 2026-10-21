package com.avengers.cardgame.command;

import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.UserService;

/**
 * Создаёт {@link CommandRegistry} со всеми командами бота.
 */
public class CommandRegistryFactory {

    /** Сервис пользователей. */
    private final UserService userService;

    /** Сервис карточек. */
    private final CardService cardService;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Генератор страниц инвентаря. */
    private final InventoryRenderer inventoryRenderer;

    /**
     * @param userService        сервис пользователей
     * @param cardService        сервис карточек
     * @param userCardRepository репозиторий карточек пользователей
     * @param inventoryRenderer  генератор страниц инвентаря
     */
    public CommandRegistryFactory(
            UserService userService,
            CardService cardService,
            UserCardRepository userCardRepository,
            InventoryRenderer inventoryRenderer
    ) {
        this.userService = userService;
        this.cardService = cardService;
        this.userCardRepository = userCardRepository;
        this.inventoryRenderer = inventoryRenderer;
    }

    /**
     * Создаёт и наполняет реестр команд.
     *
     * @return готовый реестр
     */
    public CommandRegistry create() {
        CommandRegistry registry = new CommandRegistry();

        registry.register("/start", wrap(new StartCommand()));
        registry.register("/help", new HelpCommand());
        registry.register("/profile", wrap(new ProfileCommand(userService, userCardRepository)));
        registry.register("/daily", wrap(new DailyCommand(cardService)));
        registry.register("/inventory", wrap(new InventoryCommand(inventoryRenderer)));
        registry.register("/setname", wrap(new SetNameCommand(userService)));
        registry.register("/setbio", wrap(new SetBioCommand(userService)));

        return registry;
    }

    /**
     * Оборачивает команду в {@link RegisteringCommand}.
     *
     * @param command команда
     * @return обёрнутый обработчик
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommand(command, userService);
    }
}