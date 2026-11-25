package com.avengers.cardgame.command;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.Optional;

/**
 * Обработчик команды /disassemble_pick (выбор карточки и вариантов количества).
 */
public class DisassemblePickCommandHandler implements CommandHandler {

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public DisassemblePickCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.heroRepository = heroRepository;
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Long> parsedHeroId = CommandArguments.parseId(context.argument());
        if (parsedHeroId.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidHeroId(), context.chatId());
            return;
        }
        long heroId = parsedHeroId.get();
        Hero hero = heroRepository.findById(heroId).orElse(null);
        if (hero == null) {
            context.bot().sendMessage(disassembleRenderer.renderUnknownHero(), context.chatId());
            return;
        }
        int count = inventoryLoader.countCards(context.userId(), heroId);
        if (count <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderCardNotInCollection(), context.chatId());
            return;
        }
        context.bot().sendMessage(disassembleRenderer.renderQuantity(hero, count), context.chatId());
    }
}
