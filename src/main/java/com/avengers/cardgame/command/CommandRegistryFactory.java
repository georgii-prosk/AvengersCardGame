package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.DailyRewardService;
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

    /** Сервис выдачи ежедневной награды. */
    private final DailyRewardService dailyRewardService;

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
     * @param dailyRewardService сервис выдачи ежедневной награды
     * @param marketService      сервис рынка
     * @param userCardRepository репозиторий карточек
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public CommandRegistryFactory(UserService userService, CardService cardService,
                                  DailyRewardService dailyRewardService, MarketService marketService,
                                  UserCardRepository userCardRepository,
                                  HeroRepository heroRepository,
                                  GameConfig gameConfig) {
        this.userService = userService;
        this.cardService = cardService;
        this.dailyRewardService = dailyRewardService;
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
        registry.register("/daily", wrap(new DailyCommandHandler(dailyRewardService)));

        InventoryCommandHandler inventoryHandler =
                new InventoryCommandHandler(userCardRepository, heroRepository);
        registry.register("/inventory", wrap(inventoryHandler));
        registry.register("/inventory_page", inventoryHandler::handlePage);

        registry.register("/view_card", new ViewCardCommandHandler(heroRepository, userCardRepository));

        DisassembleCommandHandler disassembleHandler = new DisassembleCommandHandler(
                userCardRepository, heroRepository, cardService, gameConfig);
        registry.register("/disassemble", wrap(disassembleHandler));
        registry.register("/disassemble_page", disassembleHandler::handlePage);
        registry.register("/disassemble_pick", disassembleHandler::handlePick);
        registry.register("/disassemble_qty", disassembleHandler::handleQuantity);
        registry.register("/disassemble_confirm", disassembleHandler::handleConfirm);

        CraftCommandHandler craftHandler = new CraftCommandHandler(cardService, userService, gameConfig);
        registry.register("/craft", wrap(craftHandler));
        registry.register("/craft_make", craftHandler::handleMake);

        SellCommandHandler sellHandler = new SellCommandHandler(
                userCardRepository, heroRepository, marketService, gameConfig);
        registry.register("/sell", wrap(sellHandler));
        registry.register("/sell_page", sellHandler::handlePage);
        registry.register("/sell_pick", sellHandler::handlePick);
        registry.register("/sell_price", sellHandler::handlePrice);
        registry.register("/sell_confirm", sellHandler::handleConfirm);

        MarketCommandHandler marketHandler = new MarketCommandHandler(
                marketService, heroRepository, userService, gameConfig);
        registry.register("/market", wrap(marketHandler));
        registry.register("/market_page", marketHandler::handlePage);
        registry.register("/market_filter", marketHandler::handleFilter);
        registry.register("/market_buy", marketHandler::handleBuy);
        registry.register("/market_buy_confirm", marketHandler::handleBuyConfirm);
        registry.register("/market_mine", marketHandler::handleMine);
        registry.register("/market_cancel", marketHandler::handleCancel);

        registry.register("/cancel", new CancelCommandHandler());

        return registry;
    }

    /**
     * Оборачивает команду в декоратор, который регистрирует пользователя перед выполнением.
     *
     * @param command обрабатываемая команда
     * @return команда с регистрацией пользователя
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommandHandler(command, userService);
    }
}
