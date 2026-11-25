package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.List;

/**
 * Обработчик команды /disassemble (первая страница коллекции для разбора).
 */
public class DisassembleCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public DisassembleCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                     GameConfig gameConfig) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(
                disassembleRenderer.renderSelection(items, FIRST_PAGE, gameConfig.shardsEmoji()),
                context.chatId());
    }
}
