package com.avengers.cardgame.command;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.presentation.InventoryItem;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Загружает коллекцию пользователя и преобразует её в отсортированные элементы инвентаря.
 */
final class InventoryQuery {

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    InventoryQuery(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
    }

    /**
     * Загружает карточки пользователя, группирует их по героям и сортирует.
     * <p>
     * Сортировка происходит по редкости (в порядке убывания),
     * а при равенстве редкости — по имени героя (в алфавитном порядке).
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список записей (герой и количество его копий)
     */
    List<InventoryItem> load(long userId) {
        Map<Long, Integer> countsByHero = new LinkedHashMap<>();
        for (UserCard card : userCardRepository.findByUserId(userId)) {
            countsByHero.merge(card.heroId(), card.count(), Integer::sum);
        }

        List<InventoryItem> items = new ArrayList<>();
        countsByHero.forEach((heroId, count) -> {
            if (count > 0) {
                heroRepository.findById(heroId).ifPresent(hero -> items.add(new InventoryItem(hero, count)));
            }
        });

        items.sort(Comparator.comparing((InventoryItem item) -> item.hero().rarity()).reversed()
                .thenComparing(item -> item.hero().name()));
        return items;
    }

    /**
     * Возвращает количество копий конкретной карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    int count(long userId, long heroId) {
        return userCardRepository.findByUserId(userId).stream()
                .filter(card -> card.heroId() == heroId)
                .mapToInt(UserCard::count)
                .sum();
    }
}
