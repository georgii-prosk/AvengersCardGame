package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /disassemble (первая страница коллекции для разбора).
 */
public class DisassembleCommandHandler implements CommandHandler {

    /** Первая страница. */
    private static final int FIRST_PAGE = 1;

    /** Отправитель страницы коллекции. */
    private final DisassembleSelectionSender selectionSender;

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public DisassembleCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                     GameConfig gameConfig) {
        this.selectionSender = new DisassembleSelectionSender(userCardRepository, heroRepository, gameConfig);
    }

    @Override
    public void handle(CommandContext context) {
        selectionSender.send(context, FIRST_PAGE);
    }
}
