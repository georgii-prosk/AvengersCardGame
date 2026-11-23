package com.avengers.cardgame.command;

import com.avengers.cardgame.presentation.SellRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /sell_page (переключение страницы коллекции для продажи).
 */
public class SellPageCommandHandler implements CommandHandler {

    /** Отправитель страницы коллекции. */
    private final SellSelectionSender selectionSender;

    /** Рендерер выставления лота. */
    private final SellRenderer sellRenderer = new SellRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public SellPageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.selectionSender = new SellSelectionSender(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(sellRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        selectionSender.send(context, page.get());
    }
}
