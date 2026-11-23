package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;

import java.util.List;

/**
 * Общая логика отправки страницы коллекции для разбора.
 */
class DisassembleSelectionSender {

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
    DisassembleSelectionSender(UserCardRepository userCardRepository, HeroRepository heroRepository,
                               GameConfig gameConfig) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
        this.gameConfig = gameConfig;
    }

    /**
     * Отправляет страницу коллекции для разбора.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    void send(CommandContext context, int page) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(
                disassembleRenderer.renderSelection(items, page, gameConfig.shardsEmoji()), context.chatId());
    }
}
