package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.command.CommandNames;
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
                                  MarketService marketService,
                                  UserCardRepository userCardRepository,
                                  HeroRepository heroRepository,
                                  GameConfig gameConfig) {
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

        registry.register(CommandNames.START, wrap(new StartCommandHandler()));
        registry.register(CommandNames.HELP, new HelpCommandHandler());
        registry.register(CommandNames.PROFILE, wrap(new ProfileCommandHandler(userService)));
        registry.register(CommandNames.SET_NAME, wrap(new SetNameCommandHandler(userService)));
        registry.register(CommandNames.SET_BIO, wrap(new SetBioCommandHandler(userService)));
        registry.register(CommandNames.DAILY, wrap(new DailyCommandHandler(cardService)));

        registry.register(CommandNames.INVENTORY, wrap(new InventoryCommandHandler(userCardRepository, heroRepository)));
        registry.register(CommandNames.INVENTORY_PAGE, new InventoryPageCommandHandler(userCardRepository, heroRepository));

        registry.register(CommandNames.VIEW_CARD, new ViewCardCommandHandler(heroRepository, userCardRepository));

        registry.register(CommandNames.DISASSEMBLE, wrap(new DisassembleCommandHandler(
                userCardRepository, heroRepository, gameConfig)));
        registry.register(CommandNames.DISASSEMBLE_PAGE, new DisassemblePageCommandHandler(
                userCardRepository, heroRepository, gameConfig));
        registry.register(CommandNames.DISASSEMBLE_PICK, new DisassemblePickCommandHandler(userCardRepository, heroRepository));
        registry.register(CommandNames.DISASSEMBLE_QTY, new DisassembleQuantityCommandHandler(
                userCardRepository, heroRepository, gameConfig));
        registry.register(CommandNames.DISASSEMBLE_CONFIRM, new DisassembleConfirmCommandHandler(
                cardService, heroRepository, gameConfig));

        registry.register(CommandNames.CRAFT, wrap(new CraftCommandHandler(userService, gameConfig)));
        registry.register(CommandNames.CRAFT_MAKE, new CraftMakeCommandHandler(cardService));

        registry.register(CommandNames.SELL, wrap(new SellCommandHandler(userCardRepository, heroRepository)));
        registry.register(CommandNames.SELL_PAGE, new SellPageCommandHandler(userCardRepository, heroRepository));
        registry.register(CommandNames.SELL_PICK, new SellPickCommandHandler(userCardRepository, heroRepository, gameConfig));
        registry.register(CommandNames.SELL_PRICE, new SellPriceCommandHandler(heroRepository, gameConfig));
        registry.register(CommandNames.SELL_CONFIRM, new SellConfirmCommandHandler(marketService, heroRepository, gameConfig));

        registry.register(CommandNames.MARKET, wrap(new MarketCommandHandler(marketService, gameConfig)));
        registry.register(CommandNames.MARKET_PAGE, new MarketPageCommandHandler(marketService, gameConfig));
        registry.register(CommandNames.MARKET_FILTER, new MarketFilterCommandHandler(marketService, gameConfig));
        registry.register(CommandNames.MARKET_BUY, new MarketBuyCommandHandler(marketService, gameConfig));
        registry.register(CommandNames.MARKET_BUY_CONFIRM, new MarketBuyConfirmCommandHandler(
                marketService, heroRepository, userService, gameConfig));
        registry.register(CommandNames.MARKET_MINE, new MarketMineCommandHandler(marketService, gameConfig));
        registry.register(CommandNames.MARKET_CANCEL, new MarketCancelCommandHandler(marketService, heroRepository));

        registry.register(CommandNames.CANCEL, new CancelCommandHandler());

        return registry;
    }

    /**
     * Оборачивает команду декоратором, который регистрирует пользователя перед выполнением.
     *
     * @param command обрабатываемая команда
     * @return команда с регистрацией пользователя
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}
