package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Команда /inventory: показывает коллекцию карточек первой страницей.
 */
public class InventoryCommandHandler implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер для страниц инвентаря. */
    private final InventoryRenderer renderer = new InventoryRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public InventoryCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        sendPage(context, FIRST_PAGE);
    }

    /**
     * Открывает выбранную страницу коллекции.
     *
     * @param context контекст команды
     */
    public void handlePage(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(new Message("Некорректный номер страницы."), context.chatId());
            return;
        }
        sendPage(context, page.get());
    }

    /**
     * Отправляет страницу коллекции.
     *
     * @param context контекст команды
     * @param page    номер страницы
     */
    private void sendPage(CommandContext context, int page) {
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(renderer.render(items, page), context.chatId());
    }
}
