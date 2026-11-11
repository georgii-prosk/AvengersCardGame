package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.List;

/**
 * Команда /inventory_page: переключает страницы инвентаря.
 */
public class InventoryPageCommandHandler implements CommandHandler {

    /** Загрузчик элементов инвентаря. */
    private final InventoryQuery inventoryQuery;

    /** Рендерер для страниц инвентаря. */
    private final InventoryRenderer renderer = new InventoryRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public InventoryPageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.inventoryQuery = new InventoryQuery(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        int page;
        try {
            page = Integer.parseInt(context.argument());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message("Некорректный номер страницы."), context.chatId());
            return;
        }

        List<InventoryItem> items = inventoryQuery.load(context.userId());
        context.bot().sendMessage(renderer.render(items, page), context.chatId());
    }
}
