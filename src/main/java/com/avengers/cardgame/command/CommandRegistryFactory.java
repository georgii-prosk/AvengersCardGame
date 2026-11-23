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

        registry.register("/start", wrap(new StartCommandHandler()));
        registry.register("/help", new HelpCommandHandler());
        registry.register("/profile", wrap(new ProfileCommandHandler(userService)));
        registry.register("/setname", wrap(new SetNameCommandHandler(userService)));
        registry.register("/setbio", wrap(new SetBioCommandHandler(userService)));
        registry.register("/daily", wrap(new DailyCommandHandler(cardService)));

        registry.register("/inventory", wrap(new InventoryCommandHandler(userCardRepository, heroRepository)));
        registry.register("/inventory_page", new InventoryPageCommandHandler(userCardRepository, heroRepository));

        registry.register("/view_card", new ViewCardCommandHandler(heroRepository, userCardRepository));

        registry.register("/disassemble", wrap(new DisassembleCommandHandler(
                userCardRepository, heroRepository, gameConfig)));
        registry.register("/disassemble_page", new DisassemblePageCommandHandler(
                userCardRepository, heroRepository, gameConfig));
        registry.register("/disassemble_pick", new DisassemblePickCommandHandler(userCardRepository, heroRepository));
        registry.register("/disassemble_qty", new DisassembleQuantityCommandHandler(
                userCardRepository, heroRepository, gameConfig));
        registry.register("/disassemble_confirm", new DisassembleConfirmCommandHandler(
                cardService, heroRepository, gameConfig));

        registry.register("/craft", wrap(new CraftCommandHandler(userService, gameConfig)));
        registry.register("/craft_make", new CraftMakeCommandHandler(cardService));

        registry.register("/sell", wrap(new SellCommandHandler(userCardRepository, heroRepository)));
        registry.register("/sell_page", new SellPageCommandHandler(userCardRepository, heroRepository));
        registry.register("/sell_pick", new SellPickCommandHandler(userCardRepository, heroRepository, gameConfig));
        registry.register("/sell_price", new SellPriceCommandHandler(heroRepository, gameConfig));
        registry.register("/sell_confirm", new SellConfirmCommandHandler(marketService, heroRepository, gameConfig));

        registry.register("/market", wrap(new MarketCommandHandler(marketService, gameConfig)));
        registry.register("/market_page", new MarketPageCommandHandler(marketService, gameConfig));
        registry.register("/market_filter", new MarketFilterCommandHandler(marketService, gameConfig));
        registry.register("/market_buy", new MarketBuyCommandHandler(marketService, gameConfig));
        registry.register("/market_buy_confirm", new MarketBuyConfirmCommandHandler(
                marketService, heroRepository, userService, gameConfig));
        registry.register("/market_mine", new MarketMineCommandHandler(marketService, gameConfig));
        registry.register("/market_cancel", new MarketCancelCommandHandler(marketService, heroRepository));

        registry.register("/cancel", new CancelCommandHandler());

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