package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;

/**
 * Обработчик команды /market (первая страница рынка).
 */
public class MarketCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Отправитель страницы рынка. */
    private final MarketPageSender pageSender;

    /**
     * @param marketService сервис рынка
     * @param gameConfig    игровая конфигурация
     */
    public MarketCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.pageSender = new MarketPageSender(marketService, gameConfig);
    }

    @Override
    public void handle(CommandContext context) {
        pageSender.send(context, null, FIRST_PAGE);
    }
}
