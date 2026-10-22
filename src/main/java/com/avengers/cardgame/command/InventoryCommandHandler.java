package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.BotException;

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
    public void handle(CommandContext context) throws BotException {
        context.bot().sendMessage(renderer.render(context.userId(), FIRST_PAGE), context.chatId());
    }
}