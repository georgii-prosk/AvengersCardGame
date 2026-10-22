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
    private final InventoryRenderer renderer;

    /**
     * @param userService        сервис пользователей
     * @param cardService        сервис карточек
     * @param userCardRepository репозиторий карточек пользователей
     * @param renderer           генератор страниц инвентаря
     */
    public CommandRegistryFactory(
            UserService userService,
            CardService cardService,
            UserCardRepository userCardRepository,
            InventoryRenderer renderer
    ) {
        this.userService = userService;
        this.cardService = cardService;
        this.userCardRepository = userCardRepository;
        this.renderer = renderer;
    }

    /**
     * Создаёт и наполняет реестр команд.
     *
     * @return готовый реестр
     */
    public CommandRegistry create() {
        CommandRegistry registry = new CommandRegistry();

        registry.register("/start", wrap(new StartCommandHandler()));
        registry.register("/help", new HelpCommandHandler());
        registry.register("/profile", wrap(new ProfileCommandHandler(userService)));
        registry.register("/setname", wrap(new SetNameCommandHandler(userService)));
        registry.register("/setbio", wrap(new SetBioCommandHandler(userService)));
        registry.register("/daily", wrap(new DailyCommandHandler(cardService)));
        registry.register("/inventory", wrap(new InventoryCommandHandler(renderer)));

        return registry;
    }

    /**
     * Оборачивает команду в {@link RegisteringCommandHandler}.
     *
     * @param command команда
     * @return обёрнутый обработчик
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}