package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Обработчик команды /market_filter (применение фильтра по редкости).
 */
public class MarketFilterCommandHandler implements CommandHandler {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(MarketFilterCommandHandler.class);

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Отправитель страницы рынка. */
    private final MarketPageSender pageSender;

    /**
     * @param marketService сервис рынка
     * @param gameConfig    игровая конфигурация
     */
    public MarketFilterCommandHandler(MarketService marketService, GameConfig gameConfig) {
        this.pageSender = new MarketPageSender(marketService, gameConfig);
    }

    @Override
    public void handle(CommandContext context) {
        HeroRarity rarity;
        try {
            rarity = pageSender.parseFilter(context.argument());
        } catch (IllegalArgumentException exception) {
            log.error("Unknown market filter argument: {}", context.argument(), exception);
            return;
        }
        pageSender.send(context, rarity, FIRST_PAGE);
    }
}
