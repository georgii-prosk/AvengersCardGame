package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.Message;

public class InventoryPageCommandHandler implements CommandHandler {
    private final InventoryRenderer renderer;
    public InventoryPageCommandHandler(InventoryRenderer renderer) { this.renderer = renderer; }

    @Override
    public void handle(CommandContext context) throws BotException {
        int page;
        try {
            page = Integer.parseInt(context.argument());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message("Некорректный номер страницы."), context.chatId());
            return;
        }
        context.bot().sendMessage(renderer.render(context.userId(), page), context.chatId());
    }
}
