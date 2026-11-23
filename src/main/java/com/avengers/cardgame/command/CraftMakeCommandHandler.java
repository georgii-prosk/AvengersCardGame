package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.presentation.CraftRenderer;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CraftResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Обработчик команды /craft_make (крафт карточки выбранной редкости).
 */
public class CraftMakeCommandHandler implements CommandHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(CraftMakeCommandHandler.class);

    /** Сервис карточек. */
    private final CardService cardService;

    /** Рендерер крафта. */
    private final CraftRenderer craftRenderer = new CraftRenderer();

    /**
     * @param cardService сервис карточек
     */
    public CraftMakeCommandHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        HeroRarity rarity;
        try {
            rarity = HeroRarity.valueOf(context.argument().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            log.error("Unknown craft rarity argument: {}", context.argument(), exception);
            return;
        }

        try {
            CraftResult result = cardService.craft(context.userId(), rarity);
            context.bot().sendMessage(craftRenderer.renderCraftSuccess(result), context.chatId());
        } catch (InsufficientShardsException exception) {
            context.bot().sendMessage(craftRenderer.renderInsufficientShards(
                    exception.required(), exception.available(), exception.missing()), context.chatId());
        }
    }
}
