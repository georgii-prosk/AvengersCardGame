package com.avengers.cardgame.command;

/**
 * Команда /inventory: показывает коллекцию карточек первой страницей.
 */
public class InventoryCommand implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    /** Генератор страниц инвентаря. */
    private final InventoryRenderer renderer;

    /**
     * @param renderer генератор страниц инвентаря
     */
    public InventoryCommand(InventoryRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(renderer.render(context.userId(), FIRST_PAGE), context.chatId());
    }
}
