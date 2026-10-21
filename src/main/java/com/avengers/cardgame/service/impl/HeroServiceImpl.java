package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.HeroService;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Реализация {@link HeroService} на основе {@link HeroRepository}.
 * <p>
 * Бросок {@code [0, сумма весов)} сопоставляется с редкостью по накопленным
 * весам. При стандартных весах 60/25/12/3 это диапазоны 0–59 (обычная),
 * 60–84 (редкая), 85–96 (эпическая) и 97–99 (легендарная).
 */
public class HeroServiceImpl implements HeroService {

    /** Сообщение об ошибке, если справочник героев пуст. */
    private static final String NO_HEROES_MESSAGE = "No heroes available in repository";

    /** Сообщение об ошибке, если бросок не попал ни в одну редкость. */
    private static final String ROLL_OUT_OF_RANGE_MESSAGE = "Rarity roll is out of range: ";

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Источник случайности. */
    private final Random random;

    /** Сумма весов всех редкостей. */
    private final int totalWeight;

    /**
     * @param heroRepository репозиторий героев
     * @param random         источник случайности
     */
    public HeroServiceImpl(HeroRepository heroRepository, Random random) {
        this.heroRepository = heroRepository;
        this.random = random;
        this.totalWeight = Arrays.stream(HeroRarity.values()).mapToInt(HeroRarity::weight).sum();
    }

    @Override
    public Hero getRandomHero() {
        HeroRarity rarity = rollRarity();

        List<Hero> candidates = heroRepository.findByRarity(rarity);
        if (candidates.isEmpty()) {
            // Для выпавшей редкости героев нет — берём любого, чтобы выдача не ломалась.
            candidates = heroRepository.findAll();
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException(NO_HEROES_MESSAGE);
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * Определяет редкость случайным броском с учётом весов.
     *
     * @return выпавшая редкость
     */
    private HeroRarity rollRarity() {
        int roll = random.nextInt(totalWeight);
        int threshold = 0;
        for (HeroRarity rarity : HeroRarity.values()) {
            threshold += rarity.weight();
            if (roll < threshold) {
                return rarity;
            }
        }
        throw new IllegalStateException(ROLL_OUT_OF_RANGE_MESSAGE + roll);
    }
}
