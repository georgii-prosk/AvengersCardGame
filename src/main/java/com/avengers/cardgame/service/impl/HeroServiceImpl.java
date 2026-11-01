package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.service.HeroService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Реализация {@link HeroService} на основе {@link HeroRepository}.
 */
public class HeroServiceImpl implements HeroService {
    private static final String ROLL_OUT_OF_RANGE_MESSAGE = "Rarity roll is out of range: ";

    /** Веса для выпадения каждой редкости. */
    private static final Map<HeroRarity, Integer> WEIGHTS = Map.of(
            HeroRarity.COMMON, 60,
            HeroRarity.RARE, 25,
            HeroRarity.EPIC, 12,
            HeroRarity.LEGENDARY, 3
    );

    private final HeroRepository heroRepository;
    private final Random random;
    private final int totalWeight;

    public HeroServiceImpl(HeroRepository heroRepository, Random random) {
        this.heroRepository = heroRepository;
        this.random = random;
        this.totalWeight = WEIGHTS.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public Optional<Hero> getRandomHero() {
        HeroRarity rarity = rollRarity();

        List<Hero> candidates = heroRepository.findByRarity(rarity);
        if (candidates.isEmpty()) {
            candidates = heroRepository.findAll();
        }
        if (candidates.isEmpty()) {
            return Optional.empty(); // <--- Возвращаем пустой Optional вместо исключения
        }
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }

    private HeroRarity rollRarity() {
        int roll = random.nextInt(totalWeight);
        int threshold = 0;
        for (HeroRarity rarity : HeroRarity.values()) {
            threshold += WEIGHTS.get(rarity);
            if (roll < threshold) {
                return rarity;
            }
        }
        throw new IllegalStateException(ROLL_OUT_OF_RANGE_MESSAGE + roll);
    }
}