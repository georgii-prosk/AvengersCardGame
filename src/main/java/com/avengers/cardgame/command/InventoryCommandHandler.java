package com.avengers.cardgame.command;

import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.List;

/**
 * Команда /inventory: показывает коллекцию карточек первой страницей.
 */
public class InventoryCommandHandler implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Загрузчик элементов инвентаря. */
    private final InventoryQuery inventoryQuery;

    /** Рендерер для страниц инвентаря. */
    private final InventoryRenderer renderer = new InventoryRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public InventoryCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.inventoryQuery = new InventoryQuery(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        List<InventoryItem> items = loadAndSortItems(context.userId());
        context.bot().sendMessage(renderer.render(items, FIRST_PAGE), context.chatId());
    }

    /**
     * Загружает и сортирует элементы инвентаря пользователя.
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список записей
     */
    List<InventoryItem> loadAndSortItems(long userId) {
        return inventoryQuery.load(userId);
    }
}
