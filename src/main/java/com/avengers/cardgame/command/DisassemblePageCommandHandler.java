package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.presentation.DisassembleRenderer;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryLoader;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.command.CommandArguments;

import java.util.List;
import java.util.Optional;

/**
 * Обработчик команды /disassemble_page (переключение страницы коллекции для разбора).
 */
public class DisassemblePageCommandHandler implements CommandHandler {

    /** Загрузчик коллекции карточек. */
    private final InventoryLoader inventoryLoader;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Рендерер разбора карточек. */
    private final DisassembleRenderer disassembleRenderer = new DisassembleRenderer();

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     * @param gameConfig         игровая конфигурация
     */
    public DisassemblePageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository,
                                         GameConfig gameConfig) {
        this.inventoryLoader = new InventoryLoader(userCardRepository, heroRepository);
        this.gameConfig = gameConfig;
    }

    @Override
    public void handle(CommandContext context) {
        Optional<Integer> page = CommandArguments.parseNumber(context.argument());
        if (page.isEmpty()) {
            context.bot().sendMessage(disassembleRenderer.renderInvalidPage(), context.chatId());
            return;
        }
        List<InventoryItem> items = inventoryLoader.loadAndSortItems(context.userId());
        context.bot().sendMessage(
                disassembleRenderer.renderSelection(items, page.get(), gameConfig.shardsEmoji()),
                context.chatId());
    }
}
