package com.avengers.cardgame.command;

import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /sell_page (переключение страницы коллекции для продажи).
 */
public class SellPageCommandHandler implements CommandHandler {

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public SellPageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(sellRenderer.renderSelection(items, page.get()), context.chatId());
    }
}
