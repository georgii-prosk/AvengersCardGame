package com.avengers.cardgame.command;

import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;

import java.util.List;

/**
 * Общая логика отправки страницы коллекции для продажи.
 */
class SellSelectionSender {

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    SellSelectionSender(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    /**
     * Отправляет страницу коллекции для продажи.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    void send(CommandContext context, int page) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(sellRenderer.renderSelection(items, page), context.chatId());
    }
}
