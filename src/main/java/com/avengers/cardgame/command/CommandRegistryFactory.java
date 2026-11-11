package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.CommandRegistryImpl;

/**
 * Фабрика для создания и настройки реестра команд.
 */
public class CommandRegistryFactory {
    /** Сервис пользователей. */
    private final UserService userService;

    /** Сервис карточек. */
    private final CardService cardService;

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /**
     * @param userService        сервис пользователей
     * @param cardService        сервис карточек
     * @param marketService      сервис рынка
     * @param userCardRepository репозиторий карточек
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public CommandRegistryFactory(UserService userService, CardService cardService,
                                  MarketService marketService, UserCardRepository userCardRepository,
                                  HeroRepository heroRepository, GameConfig gameConfig) {
        this.userService = userService;
        this.cardService = cardService;
        this.marketService = marketService;
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
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
        registry.register("/profile", wrap(new ProfileCommandHandler(userService, gameConfig)));
        registry.register("/setname", wrap(new SetNameCommandHandler(userService)));
        registry.register("/setbio", wrap(new SetBioCommandHandler(userService)));
        registry.register("/daily", wrap(new DailyCommandHandler(cardService)));

        registry.register("/inventory", wrap(new InventoryCommandHandler(userCardRepository, heroRepository)));
        registry.register("/inventory_page", wrap(new InventoryPageCommandHandler(userCardRepository, heroRepository)));
        registry.register("/view_card", wrap(new ViewCardCommandHandler(heroRepository, userCardRepository)));

        registerDisassembleCommands(registry);
        registerCraftCommands(registry);
        registerSellCommands(registry);
        registerMarketCommands(registry);
        registry.register("/cancel", wrap(new CancelCommandHandler()));

        return registry;
    }

    /**
     * Регистрирует команды разбора карточек.
     *
     * @param registry реестр команд
     */
    private void registerDisassembleCommands(CommandRegistry registry) {
        registry.register("/disassemble", wrap(new DisassembleCommandHandler(
                cardService, userCardRepository, heroRepository, DisassembleCommandHandler.Step.START)));
        registry.register("/disassemble_page", wrap(new DisassembleCommandHandler(
                cardService, userCardRepository, heroRepository, DisassembleCommandHandler.Step.PAGE)));
        registry.register("/disassemble_pick", wrap(new DisassembleCommandHandler(
                cardService, userCardRepository, heroRepository, DisassembleCommandHandler.Step.PICK)));
        registry.register("/disassemble_qty", wrap(new DisassembleCommandHandler(
                cardService, userCardRepository, heroRepository, DisassembleCommandHandler.Step.QUANTITY)));
        registry.register("/disassemble_confirm", wrap(new DisassembleCommandHandler(
                cardService, userCardRepository, heroRepository, DisassembleCommandHandler.Step.CONFIRM)));
    }

    /**
     * Регистрирует команды крафта карточек.
     *
     * @param registry реестр команд
     */
    private void registerCraftCommands(CommandRegistry registry) {
        registry.register("/craft", wrap(new CraftCommandHandler(cardService, CraftCommandHandler.Step.MENU)));
        registry.register("/craft_make", wrap(new CraftCommandHandler(cardService, CraftCommandHandler.Step.MAKE)));
    }

    /**
     * Регистрирует команды продажи карточек.
     *
     * @param registry реестр команд
     */
    private void registerSellCommands(CommandRegistry registry) {
        registry.register("/sell", wrap(new SellCommandHandler(
                marketService, userCardRepository, heroRepository, gameConfig, SellCommandHandler.Step.START)));
        registry.register("/sell_page", wrap(new SellCommandHandler(
                marketService, userCardRepository, heroRepository, gameConfig, SellCommandHandler.Step.PAGE)));
        registry.register("/sell_pick", wrap(new SellCommandHandler(
                marketService, userCardRepository, heroRepository, gameConfig, SellCommandHandler.Step.PICK)));
        registry.register("/sell_price", wrap(new SellCommandHandler(
                marketService, userCardRepository, heroRepository, gameConfig, SellCommandHandler.Step.PRICE)));
        registry.register("/sell_confirm", wrap(new SellCommandHandler(
                marketService, userCardRepository, heroRepository, gameConfig, SellCommandHandler.Step.CONFIRM)));
    }

    /**
     * Регистрирует команды рынка.
     *
     * @param registry реестр команд
     */
    private void registerMarketCommands(CommandRegistry registry) {
        registry.register("/market", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.START)));
        registry.register("/market_page", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.PAGE)));
        registry.register("/market_buy", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.BUY)));
        registry.register("/market_buy_confirm", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.BUY_CONFIRM)));
        registry.register("/market_mine", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.MINE)));
        registry.register("/market_mine_page", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.MINE_PAGE)));
        registry.register("/market_cancel", wrap(new MarketCommandHandler(
                marketService, heroRepository, gameConfig, MarketCommandHandler.Step.CANCEL)));
    }

    /**
     * Оборачивает команду в декоратор, который регистрирует пользователя перед выполнением.
     *
     * @param command обработчик команды
     * @return обёрнутый обработчик
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}
