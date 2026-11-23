package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /disassemble_page (переключение страницы коллекции для разбора).
 */
public class DisassemblePageCommandHandler implements CommandHandler {

    /** Отправитель страницы коллекции. */
    private final DisassembleSelectionSender selectionSender;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public DisassemblePageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                         GameConfig gameConfig) {
        this.selectionSender = new DisassembleSelectionSender(userCardRepository, heroRepository, gameConfig);
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        selectionSender.send(context, page.get());
    }
}
