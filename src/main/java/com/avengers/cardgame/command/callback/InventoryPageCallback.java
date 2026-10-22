package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.command.InventoryRenderer;
import com.avengers.cardgame.exception.BotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработчик кнопок пагинации инвентаря ({@code page:<номер>}).
 */
public class InventoryPageCallback implements CallbackHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(InventoryPageCallback.class);

    /** Генератор страниц инвентаря. */
    private final InventoryRenderer renderer;

    /**
     * @param renderer генератор страниц инвентаря
     */
    public InventoryPageCallback(InventoryRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(CallbackContext context) throws BotException {
        int page;
        try {
            page = Integer.parseInt(context.argument());
        } catch (NumberFormatException e) {
            log.warn("Invalid page in callback data: {}", context.callbackData());
            return;
        }
        context.bot().sendMessage(renderer.render(context.userId(), page), context.chatId());
    }
}
