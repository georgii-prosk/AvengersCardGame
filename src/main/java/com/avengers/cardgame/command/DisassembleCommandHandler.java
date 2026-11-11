package com.avengers.cardgame.command;

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
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

/**
 * Обработчик разбора карточек на кусочки.
 * <p>
 * Команда состоит из шагов: /disassemble, /disassemble_pick, /disassemble_qty,
 * /disassemble_confirm (и служебного /disassemble_page).
 */
public class DisassembleCommandHandler implements CommandHandler {

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Заголовок списка карточек для разбора. */
    private static final String INVENTORY_HEADER = "♻️ Выбери карточку для разбора (страница %d/%d):";

    /** Команда выбора карточки. */
    private static final String PICK_COMMAND = "/disassemble_pick";

    /** Команда переключения страницы. */
    private static final String PAGE_COMMAND = "/disassemble_page";

    /** Текст ответа на неизвестную или устаревшую карточку. */
    private static final String CARD_NOT_FOUND_MESSAGE = "Такой карточки не существует.";

    /** Текст ответа, если карточки нет в коллекции. */
    private static final String CARD_NOT_OWNED_MESSAGE = "У тебя нет этой карточки.";

    /** Текст ответа при некорректных аргументах. */
    private static final String INVALID_ARGUMENT_MESSAGE = "Некорректный формат команды.";

    /** Сервис карточек. */
    private final CardService cardService;

    /** Загрузчик элементов инвентаря. */
    private final InventoryQuery inventoryQuery;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Рендерер инвентаря. */
    private final InventoryRenderer inventoryRenderer = new InventoryRenderer();

    /** Рендерер карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /** Шаг обработки команды. */
    private final Step step;

    /**
     * Шаги команды разбора.
     */
    public enum Step {
        /** Показ списка карточек. */
        START,
        /** Переключение страницы списка. */
        PAGE,
        /** Выбор карточки. */
        PICK,
        /** Выбор количества. */
        QUANTITY,
        /** Подтверждение и выполнение разбора. */
        CONFIRM
    }

    /**
     * @param cardService        сервис карточек
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param step               шаг обработки
     */
    public DisassembleCommandHandler(CardService cardService, UserCardRepository userCardRepository,
                                     HeroRepository heroRepository, @NotNull Step step) {
        this.cardService = cardService;
        this.inventoryQuery = new InventoryQuery(userCardRepository, heroRepository);
        this.heroRepository = heroRepository;
        this.step = step;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        switch (step) {
            case START -> showInventory(context, FIRST_PAGE);
            case PAGE -> handlePage(context);
            case PICK -> handlePick(context);
            case QUANTITY -> handleQuantity(context);
            case CONFIRM -> handleConfirm(context);
        }
    }

    /**
     * Показывает страницу коллекции для разбора.
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
     * Показывает варианты количества для выбранной карточки.
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

        int available = inventoryQuery.count(context.userId(), heroId);
        if (available <= 0) {
            context.bot().sendMessage(new Message(CARD_NOT_OWNED_MESSAGE), context.chatId());
            return;
        }

        int shardsPerCard = hero.get().rarity().shardsYield();
        String text = """
                ♻️ Разбор карточки
                %s %s — %s
                За одну карточку: +%d %s
                В коллекции: ×%d
                Сколько карточек разобрать?
                """.formatted(heroRenderer.emoji(hero.get().rarity()), hero.get().name(),
                heroRenderer.title(hero.get().rarity()), shardsPerCard, SHARDS_EMOJI, available);

        List<List<Button>> rows = new ArrayList<>();
        for (int amount : quantityOptions(available)) {
            rows.add(List.of(new Button("Разобрать ×" + amount,
                    "/disassemble_qty " + heroId + " " + amount)));
        }
        rows.add(List.of(new Button("❌ Отмена", "/cancel")));

        context.bot().sendMessage(new Message(text, rows), context.chatId());
    }

    /**
     * Показывает подтверждение разбора.
     *
     * @param context контекст команды
     */
    private void handleQuantity(CommandContext context) {
        long[] arguments = parseTwoLongs(context.argument());
        if (arguments == null) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }
        long heroId = arguments[0];
        long requested = arguments[1];
        if (requested <= 0) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        Optional<Hero> hero = heroRepository.findById(heroId);
        if (hero.isEmpty()) {
            context.bot().sendMessage(new Message(CARD_NOT_FOUND_MESSAGE), context.chatId());
            return;
        }
        int available = inventoryQuery.count(context.userId(), heroId);
        if (requested > available) {
            context.bot().sendMessage(new Message("У тебя только %d таких карточек.".formatted(available)),
                    context.chatId());
            return;
        }

        int totalShards = hero.get().rarity().shardsYield() * (int) requested;
        String text = """
                Подтверди разбор
                %s %s ×%d → +%d %s
                """.formatted(heroRenderer.emoji(hero.get().rarity()), hero.get().name(),
                requested, totalShards, SHARDS_EMOJI);

        List<List<Button>> rows = List.of(
                List.of(new Button("✅ Подтвердить",
                        "/disassemble_confirm " + heroId + " " + requested)),
                List.of(new Button("❌ Отмена", "/cancel"))
        );
        context.bot().sendMessage(new Message(text, rows), context.chatId());
    }

    /**
     * Выполняет разбор карточек.
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
        int count = (int) arguments[1];
        if (count <= 0) {
            context.bot().sendMessage(new Message(INVALID_ARGUMENT_MESSAGE), context.chatId());
            return;
        }

        Optional<Hero> hero = heroRepository.findById(heroId);
        try {
            int shards = cardService.disassemble(context.userId(), heroId, count);
            String heroName = hero.map(Hero::name).orElse("карточка");
            context.bot().sendMessage(new Message("""
                    ✅ Разобрано: %s ×%d
                    Получено кусочков: +%d %s
                    """.formatted(heroName, count, shards, SHARDS_EMOJI)), context.chatId());
        } catch (NotEnoughCardsException exception) {
            context.bot().sendMessage(new Message(
                    "Недостаточно карточек: доступно %d.".formatted(exception.available())), context.chatId());
        }
    }

    /**
     * Возвращает уникальные варианты количества для кнопок.
     *
     * @param available доступное количество карточек
     * @return отсортированные варианты
     */
    private List<Integer> quantityOptions(int available) {
        TreeSet<Integer> options = new TreeSet<>();
        for (int candidate : new int[]{1, 3, 5}) {
            if (candidate <= available) options.add(candidate);
        }
        options.add(available);
        return new ArrayList<>(options);
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
