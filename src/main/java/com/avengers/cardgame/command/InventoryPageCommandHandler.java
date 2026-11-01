package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.*;

/**
 * Команда /inventory_page: переключает страницы инвентаря.
 */
public class InventoryPageCommandHandler implements CommandHandler {
    private final UserCardRepository userCardRepository;
    private final HeroRepository heroRepository;
    private final InventoryRenderer renderer;

    public InventoryPageCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository, InventoryRenderer renderer) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
        this.renderer = renderer;
    }

    @Override
    public void handle(CommandContext context) {
        int page;
        try {
            page = Integer.parseInt(context.argument());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message("Некорректный номер страницы."), context.chatId());
            return;
        }

        List<InventoryItem> items = loadAndSortItems(context.userId());
        context.bot().sendMessage(renderer.render(items, page), context.chatId());
    }

    /**
     * Загружает карточки пользователя, группирует их по героям,
     * подтягивает данные героев и сортирует полученный список.
     * <p>
     * Сортировка происходит по редкости (в порядке убывания),
     * а при равенстве редкости — по имени героя (в алфавитном порядке).
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список записей (герой и количество его копий)
     */
    private List<InventoryItem> loadAndSortItems(long userId) {
        Map<Long, Integer> countsByHero = new LinkedHashMap<>();
        for (UserCard card : userCardRepository.findByUserId(userId)) {
            countsByHero.merge(card.heroId(), card.count(), Integer::sum);
        }

        List<InventoryItem> items = new ArrayList<>();
        countsByHero.forEach((heroId, count) -> {
            if (count > 0) heroRepository.findById(heroId).ifPresent(hero -> items.add(new InventoryItem(hero, count)));
        });

        items.sort(Comparator.comparing((InventoryItem item) -> item.hero().rarity()).reversed()
                .thenComparing(item -> item.hero().name()));
        return items;
    }
}