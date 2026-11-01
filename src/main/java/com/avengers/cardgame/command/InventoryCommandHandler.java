package com.avengers.cardgame.command;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.presentation.InventoryRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.*;

/**
 * Команда /inventory: показывает коллекцию карточек первой страницей.
 */
public class InventoryCommandHandler implements CommandHandler {

    /** Номер первой страницы. */
    private static final int FIRST_PAGE = 1;

    private final UserCardRepository userCardRepository;
    private final HeroRepository heroRepository;

    /** Рендерер для страниц инвентаря. */
    private final InventoryRenderer renderer = new InventoryRenderer();

    public InventoryCommandHandler(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
    }

    @Override
    public void handle(CommandContext context) {
        List<InventoryItem> items = loadAndSortItems(context.userId());
        context.bot().sendMessage(renderer.render(items, FIRST_PAGE), context.chatId());
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
    List<InventoryItem> loadAndSortItems(long userId) {
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