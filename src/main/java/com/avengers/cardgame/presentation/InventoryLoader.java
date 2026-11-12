package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Загружает коллекцию карточек пользователя для отображения.
 */
public class InventoryLoader {

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public InventoryLoader(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
    }

    /**
     * Загружает коллекцию карточек пользователя.
     * <p>
     * Записи отсортированы по редкости (в порядке убывания),
     * а при равенстве редкости — по имени героя (в алфавитном порядке).
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список записей (герой и количество его копий)
     */
    public List<InventoryItem> loadAndSortItems(long userId) {
        Map<Long, Integer> countsByHero = new LinkedHashMap<>();
        for (UserCard card : userCardRepository.findByUserId(userId)) {
            countsByHero.merge(card.heroId(), card.count(), Integer::sum);
        }
        List<InventoryItem> items = new ArrayList<>();
        countsByHero.forEach((heroId, count) -> {
            if (count > 0) {
                heroRepository.findById(heroId)
                        .ifPresent(hero -> items.add(new InventoryItem(hero, count)));
            }
        });
        items.sort(Comparator.comparing((InventoryItem item) -> item.hero().rarity()).reversed()
                .thenComparing(item -> item.hero().name()));
        return items;
    }

    /**
     * Считает количество копий указанного героя в коллекции пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    public int countCards(long userId, long heroId) {
        return userCardRepository.countByUserIdAndHeroId(userId, heroId);
    }
}
