package com.avengers.cardgame.command;

import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Команда /inventory: показывает коллекцию карточек первой страницей.
 */
public class InventoryCommandHandler implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Генератор страниц инвентаря. */
    private final InventoryRenderer renderer;

    /**
     * @param renderer генератор страниц инвентаря
     */
    public InventoryCommandHandler(InventoryRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(renderer.render(context.userId(), FIRST_PAGE), context.chatId());
    }
}