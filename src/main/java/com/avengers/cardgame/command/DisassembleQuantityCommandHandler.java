package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /disassemble_qty (подтверждение выбранного количества карточек).
 */
public class DisassembleQuantityCommandHandler implements CommandHandler {

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public DisassembleQuantityCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                             GameConfig gameConfig) {
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
    }

    @Override
    public void handle(CommandContext context) {
        List<String> parts = CommandArguments.splitParts(context.argument());
        if (parts.size() != 2) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        Optional<Long> parsedHeroId = CommandArguments.parseId(parts.getFirst());
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
        int available = inventoryLoader.countCards(context.userId(), heroId);
        if (available <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderCardNotInCollection(), context.chatId());
            return;
        }
        Optional<Integer> parsedQuantity = parseQuantity(parts.getLast(), available);
        if (parsedQuantity.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        int quantity = parsedQuantity.get();
        if (quantity <= 0) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidQuantity(), context.chatId());
            return;
        }
        if (quantity > available) {
            context.bot().sendMessage(
                    disassembleRenderer.renderNotEnoughCards(hero.name(), available), context.chatId());
            return;
        }

        int shardsToGain = quantity * gameConfig.rarity(hero.rarity()).shardsYield();
        context.bot().sendMessage(disassembleRenderer.renderConfirmation(
                hero, available, quantity, shardsToGain, gameConfig.shardsEmoji()), context.chatId());
    }

    /**
     * Преобразует аргумент количества в число.
     *
     * @param token     аргумент пользователя
     * @param available доступное количество карточек
     * @return количество карточек, если разбор удался
     */
    private Optional<Integer> parseQuantity(String token, int available) {
        if (DisassembleRenderer.ALL_ARGUMENT.equalsIgnoreCase(token)) {
            return Optional.of(available);
        }
        return CommandArguments.parseNumber(token);
    }
}
