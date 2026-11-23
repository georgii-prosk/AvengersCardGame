package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.DisassembleResult;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /disassemble_confirm (выполнение разбора карточек).
 */
public class DisassembleConfirmCommandHandler implements CommandHandler {

    /** Сервис карточек. */
    private final CardService cardService;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param cardService    сервис карточек
     * @param heroRepository репозиторий героев
     * @param gameConfig     игровая конфигурация
     */
    public DisassembleConfirmCommandHandler(CardService cardService, HeroRepository heroRepository,
                                            GameConfig gameConfig) {
        this.cardService = cardService;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        Optional<CommandArguments.IdAndNumber> parsed = CommandArguments.parseIdAndNumber(context.argument());
        if (parsed.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        long heroId = parsed.get().id();
        int quantity = parsed.get().number();

        try {
            DisassembleResult result = cardService.disassemble(context.userId(), heroId, quantity);
            context.bot().sendMessage(
                    disassembleRenderer.renderResult(result, gameConfig.shardsEmoji()), context.chatId());
        } catch (NotEnoughCardsException exception) {
            String heroName = heroRepository.findById(exception.heroId())
                    .map(Hero::name)
                    .orElse("этой карточки");
            context.bot().sendMessage(
                    disassembleRenderer.renderNotEnoughCards(heroName, exception.available()), context.chatId());
        }
    }
}
