package com.avengers.cardgame.command;

import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /sell (первая страница коллекции для продажи).
 */
public class SellCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Отправитель страницы коллекции. */
    private final SellSelectionSender selectionSender;

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public SellCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.selectionSender = new SellSelectionSender(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        selectionSender.send(context, FIRST_PAGE);
    }
}
