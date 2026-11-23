package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.presentation.MarketRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /market_page (переключение страницы рынка с фильтром).
 */
public class MarketPageCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Отправитель страницы рынка. */
    private final MarketPageSender pageSender;

    /**
     * @param marketService сервис рынка
     * @param gameConfig    игровая конфигурация
     */
    public MarketPageCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.pageSender = new MarketPageSender(marketService, gameConfig);
    }

    @Override
    public void handle(CommandContext context) {
        List<String> parts = CommandArguments.splitParts(context.argument());
        String filter = MarketRenderer.ALL_FILTER;
        int page = FIRST_PAGE;
        if (parts.size() == 1 && !parts.getFirst().isBlank()) {
            Optional<Integer> parsedPage = CommandArguments.parseNumber(parts.getFirst());
            if (parsedPage.isPresent()) {
                page = parsedPage.get();
            } else {
                filter = parts.getFirst();
            }
        } else if (parts.size() >= 2) {
            filter = parts.getFirst();
            Optional<Integer> parsedPage = CommandArguments.parseNumber(parts.get(1));
            if (parsedPage.isEmpty()) {
                pageSender.sendInvalidPage(context);
                return;
            }
            page = parsedPage.get();
        }

        HeroRarity rarity;
        try {
            rarity = pageSender.parseFilter(filter);
        } catch (IllegalArgumentException exception) {
            pageSender.sendInvalidFilter(context);
            return;
        }
        pageSender.send(context, rarity, page);
    }
}
