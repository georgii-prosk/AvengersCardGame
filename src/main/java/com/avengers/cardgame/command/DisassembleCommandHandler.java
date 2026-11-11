package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.DisassembleResult;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /disassemble и шагов разбора карточек.
 */
public class DisassembleCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Сервис карточек. */
    private final CardService cardService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param cardService        сервис карточек
     * @param gameConfig         игровая конфигурация
     */
    public DisassembleCommandHandler(UserCardRepository userCardRepository,
                                     HeroRepository heroRepository,
                                     CardService cardService,
                                     GameConfig gameConfig) {
        this.heroRepository = heroRepository;
        this.cardService = cardService;
        this.gameConfig = gameConfig;
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        sendSelection(context, FIRST_PAGE);
    }

    /**
     * Открывает выбранную страницу коллекции для разбора.
     *
     * @param context контекст команды
     */
    public void handlePage(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        sendSelection(context, page.get());
    }

    /**
     * Показывает варианты количества для выбранной карточки.
     *
     * @param context контекст команды
     */
    public void handlePick(CommandContext context) {
        Optional<Long> parsedHeroId = CommandArguments.parseId(context.argument());
        if (parsedHeroId.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidHeroId(), context.chatId());
            return;
        }
        long heroId = parsedHeroId.get();
        Hero hero = heroRepository.findById(heroId).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(disassembleRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        int count = inventoryLoader.countCards(context.userId(), heroId);
        if (count <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderCardNotInCollection(), context.chatId());
            return;
        }
        context.bot().sendMessage(disassembleRenderer.renderQuantity(hero, count), context.chatId());
    }

    /**
     * Показывает подтверждение разбора выбранного количества карточек.
     *
     * @param context контекст команды
     */
    public void handleQuantity(CommandContext context) {
        List<String> parts = CommandArguments.splitParts(context.argument());
        if (parts.size() != 2) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        Optional<Long> parsedHeroId = CommandArguments.parseId(parts.getFirst());
        if (parsedHeroId.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidHeroId(), context.chatId());
            return;
        }
        long heroId = parsedHeroId.get();
        Hero hero = heroRepository.findById(heroId).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(disassembleRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        int available = inventoryLoader.countCards(context.userId(), heroId);
        if (available <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderCardNotInCollection(), context.chatId());
            return;
        }
        Optional<Integer> parsedQuantity = parseQuantity(parts.getLast(), available);
        if (parsedQuantity.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        int quantity = parsedQuantity.get();
        if (quantity <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        if (quantity > available) {
            context.bot().sendMessage(
                    disassembleRenderer.renderNotEnoughCards(hero.name(), available), context.chatId());
            return;
        }

        int shardsToGain = quantity * hero.rarity().shardsYield();
        context.bot().sendMessage(disassembleRenderer.renderConfirmation(
                hero, available, quantity, shardsToGain, gameConfig.shardsEmoji()), context.chatId());
    }

    /**
     * Выполняет разбор карточек.
     *
     * @param context контекст команды
     * @throws AppException при ошибке разбора
     */
    public void handleConfirm(CommandContext context) throws AppException {
        Optional<CommandArguments.IdAndNumber> parsed = CommandArguments.parseIdAndNumber(context.argument());
        if (parsed.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        long heroId = parsed.get().id();
        int quantity = parsed.get().number();

        try {
            DisassembleResult result = cardService.disassemble(context.userId(), heroId, quantity);
            context.bot().sendMessage(
                    disassembleRenderer.renderResult(result, gameConfig.shardsEmoji()), context.chatId());
        } catch (NotEnoughCardsException exception) {
            String heroName = heroRepository.findById(exception.heroId())
                    .map(Hero::name)
                    .orElse("этой карточки");
            context.bot().sendMessage(
                    disassembleRenderer.renderNotEnoughCards(heroName, exception.available()), context.chatId());
        }
    }

    /**
     * Отправляет страницу коллекции для разбора.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void sendSelection(CommandContext context, int page) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(
                disassembleRenderer.renderSelection(items, page, gameConfig.shardsEmoji()), context.chatId());
    }

    /**
     * Преобразует аргумент количества в число.
     *
     * @param token     аргумент пользователя
     * @param available доступное количество карточек
     * @return количество карточек, если разбор удался
     */
    private Optional<Integer> parseQuantity(String token, int available) {
        if (DisassembleRenderer.ALL_ARGUMENT.equalsIgnoreCase(token)) {
            return Optional.of(available);
        }
        return CommandArguments.parseNumber(token);
    }
}
