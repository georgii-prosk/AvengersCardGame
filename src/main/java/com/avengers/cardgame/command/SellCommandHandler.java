package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.impl.MarketServiceImpl;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Обработчик выставления карточки на рынок.
 * <p>
 * Команда состоит из шагов: /sell, /sell_pick, /sell_price, /sell_confirm
 * (и служебного /sell_page).
 */
public class SellCommandHandler implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Заголовок списка карточек для продажи. */
    private static final String INVENTORY_HEADER = "💰 Выбери карточку для продажи (страница %d/%d):";

    /** Команда выбора карточки. */
    private static final String PICK_COMMAND = "/sell_pick";

    /** Команда переключения страницы. */
    private static final String PAGE_COMMAND = "/sell_page";

    /** Текст ответа при некорректных аргументах. */
    private static final String INVALID_ARGUMENT_MESSAGE = "Некорректный формат команды.";

    /** Текст ответа, если герой не найден. */
    private static final String CARD_NOT_FOUND_MESSAGE = "Такой карточки не существует.";

    /** Текст ответа, если карточки нет в коллекции. */
    private static final String CARD_NOT_OWNED_MESSAGE = "У тебя нет этой карточки.";

    /** Сервис рынка. */
    private final MarketService marketService;

    /** Загрузчик элементов инвентаря. */
    private final InventoryQuery inventoryQuery;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер инвентаря. */
    private final InventoryRenderer inventoryRenderer = new InventoryRenderer();

    /** Рендерер карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /** Шаг обработки команды. */
    private final Step step;

    /**
     * Шаги команды продажи.
     */
    public enum Step {
        /** Показ списка карточек. */
        START,
        /** Переключение страницы списка. */
        PAGE,
        /** Выбор карточки. */
        PICK,
        /** Выбор цены. */
        PRICE,
        /** Подтверждение и выставление лота. */
        CONFIRM
    }

    /**
     * @param marketService      сервис рынка
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     * @param step               шаг обработки
     */
    public SellCommandHandler(MarketService marketService, UserCardRepository userCardRepository,
                              HeroRepository heroRepository, GameConfig gameConfig, @NotNull Step step) {
        this.marketService = marketService;
        this.inventoryQuery = new InventoryQuery(userCardRepository, heroRepository);
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
        this.step = step;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        switch (step) {
            case START -> showInventory(context, FIRST_PAGE);
            case PAGE -> handlePage(context);
            case PICK -> handlePick(context);
            case PRICE -> handlePrice(context);
            case CONFIRM -> handleConfirm(context);
        }
    }

    /**
     * Показывает страницу коллекции для продажи.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void showInventory(CommandContext context, int page) {
        List<InventoryItem> items = inventoryQuery.load(context.userId());
        context.bot().sendMessage(inventoryRenderer.render(items, page, INVENTORY_HEADER,
                PICK_COMMAND, PAGE_COMMAND), context.chatId());
    }

    /**
     * Обрабатывает переключение страницы.
     *
     * @param context контекст команды
     */
    private void handlePage(CommandContext context) {
        try {
            showInventory(context, Integer.parseInt(context.argument()));
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
        }
    }

    /**
     * Показывает варианты цены для выбранной карточки.
     *
     * @param context контекст команды
     */
    private void handlePick(CommandContext context) {
        long heroId;
        try {
            heroId = Long.parseLong(context.argument().trim());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        Optional<Hero> hero = heroRepository.findById(heroId);
        if (hero.isEmpty()) {
            context.bot().sendMessage(new Message(CARD_NOT_FOUND_MESSAGE), context.chatId());
            return;
        }
        if (inventoryQuery.count(context.userId(), heroId) <= 0) {
            context.bot().sendMessage(new Message(CARD_NOT_OWNED_MESSAGE), context.chatId());
            return;
        }

        var rarity = hero.get().rarity();
        String text = """
                💰 Выставление лота
                %s %s — %s
                Допустимая цена: %d–%d %s
                Выбери цену:
                """.formatted(heroRenderer.emoji(rarity), hero.get().name(), heroRenderer.title(rarity),
                rarity.minPrice(), rarity.maxPrice(), gameConfig.currencyEmoji());

        List<List<Button>> rows = new ArrayList<>();
        for (int price : priceOptions(rarity.minPrice(), rarity.maxPrice())) {
            rows.add(List.of(new Button("Выставить за %d %s".formatted(price, gameConfig.currencyEmoji()),
                    "/sell_price " + heroId + " " + price)));
        }
        rows.add(List.of(new Button("❌ Отмена", "/cancel")));

        context.bot().sendMessage(new Message(text, rows), context.chatId());
    }

    /**
     * Показывает подтверждение выставления лота.
     *
     * @param context контекст команды
     */
    private void handlePrice(CommandContext context) {
        long[] arguments = parseTwoLongs(context.argument());
        if (arguments == null) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }
        long heroId = arguments[0];
        long price = arguments[1];
        if (price <= 0) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        Optional<Hero> hero = heroRepository.findById(heroId);
        if (hero.isEmpty()) {
            context.bot().sendMessage(new Message(CARD_NOT_FOUND_MESSAGE), context.chatId());
            return;
        }

        String text = """
                Подтверди выставление лота
                %s %s за %d %s
                Комиссия рынка при продаже: %d%%.
                """.formatted(heroRenderer.emoji(hero.get().rarity()), hero.get().name(),
                price, gameConfig.currencyEmoji(), MarketServiceImpl.COMMISSION_PERCENT);

        List<List<Button>> rows = List.of(
                List.of(new Button("✅ Выставить", "/sell_confirm " + heroId + " " + price)),
                List.of(new Button("❌ Отмена", "/cancel"))
        );
        context.bot().sendMessage(new Message(text, rows), context.chatId());
    }

    /**
     * Выставляет лот на рынок.
     *
     * @param context контекст команды
     */
    private void handleConfirm(CommandContext context) throws UserNotFoundException {
        long[] arguments = parseTwoLongs(context.argument());
        if (arguments == null) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }
        long heroId = arguments[0];
        int price = (int) arguments[1];

        Optional<Hero> hero = heroRepository.findById(heroId);
        try {
            marketService.createListing(context.userId(), heroId, price);
            String heroName = hero.map(Hero::name).orElse("карточка");
            context.bot().sendMessage(new Message("""
                    ✅ Лот выставлен: %s за %d %s
                    Посмотреть свои лоты: /market_mine
                    """.formatted(heroName, price, gameConfig.currencyEmoji())), context.chatId());
        } catch (InvalidPriceException exception) {
            context.bot().sendMessage(new Message(
                    "Недопустимая цена. Для этой редкости: %d–%d %s.".formatted(
                            exception.minPrice(), exception.maxPrice(), gameConfig.currencyEmoji())),
                    context.chatId());
        } catch (NotEnoughCardsException exception) {
            context.bot().sendMessage(new Message(CARD_NOT_OWNED_MESSAGE), context.chatId());
        }
    }

    /**
     * Возвращает уникальные варианты цены в допустимом диапазоне.
     *
     * @param minPrice минимальная цена
     * @param maxPrice максимальная цена
     * @return варианты цены
     */
    private List<Integer> priceOptions(int minPrice, int maxPrice) {
        Set<Integer> prices = new LinkedHashSet<>();
        prices.add(minPrice);
        prices.add((minPrice + maxPrice) / 2);
        prices.add(maxPrice);
        return new ArrayList<>(prices);
    }

    /**
     * Разбирает аргумент команды из двух чисел.
     *
     * @param argument текст аргумента
     * @return значения или {@code null}, если формат неверный
     */
    private long[] parseTwoLongs(String argument) {
        String[] parts = argument.trim().split("\\s+");
        if (parts.length != 2) return null;
        try {
            return new long[]{Long.parseLong(parts[0]), Long.parseLong(parts[1])};
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
