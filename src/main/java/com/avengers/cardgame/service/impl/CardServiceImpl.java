package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.Cooldown;
import com.avengers.cardgame.service.CraftResult;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.DisassembleResult;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.service.Success;
import com.avengers.cardgame.service.UserService;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Реализация {@link CardService}.
 */
public class CardServiceImpl implements CardService {
    /** Минимальное количество карточек, выдаваемых за одну ежедневную награду. */
    private static final int MIN_DAILY_CARDS = 1;

    /** Максимальное количество карточек, выдаваемых за одну ежедневную награду. */
    private static final int MAX_DAILY_CARDS = 3;


    /** Сервис пользователей. */
    private final UserService userService;
    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;
    /** Сервис героев. */
    private final HeroService heroService;
    /** Репозиторий героев. */
    private final HeroRepository heroRepository;
    /** Игровая конфигурация. */
    private final GameConfig gameConfig;
    /** Источник случайности. */
    private final Random random;
    /** Часы для отслеживания времени кулдауна. */
    private final Clock clock;

    /**
     * @param userService сервис пользователей
     * @param userCardRepository репозиторий карточек
     * @param heroService сервис героев
     * @param heroRepository репозиторий героев
     * @param gameConfig игровая конфигурация
     * @param random генератор случайных чисел
     * @param clock часы
     */
    public CardServiceImpl(UserService userService, UserCardRepository userCardRepository,
                           HeroService heroService, HeroRepository heroRepository,
                           GameConfig gameConfig, Random random, Clock clock) {
        this.userService = userService;
        this.userCardRepository = userCardRepository;
        this.heroService = heroService;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
        this.random = random;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DailyRewardResult grantDaily(long userId) throws UserNotFoundException {
        // Метод синхронизирован для предотвращения состояния гонки (check-then-act):
        // без этого два параллельных запроса от одного пользователя могли бы
        // одновременно пройти проверку кулдауна и выдать награду дважды.

        User user = userService.getByUserId(userId);
        LocalDateTime now = LocalDateTime.now(clock);

        Duration remaining = timeUntilNextClaim(user.lastDailyAt(), now);
        if (remaining != null) {
            return new Cooldown(remaining);
        }

        // Генерируем от 1 до 3 карточек
        int amount = MIN_DAILY_CARDS + random.nextInt(MAX_DAILY_CARDS - MIN_DAILY_CARDS + 1);
        List<Hero> heroes = new ArrayList<>(amount);
        for (int index = 0; index < amount; index++) {
            Hero hero = heroService.getRandomHero()
                    .orElseThrow(() -> new IllegalStateException("No heroes available in repository"));
            userCardRepository.addCard(userId, hero.id());
            heroes.add(hero);
        }

        userService.updateLastDailyAt(userId, now);
        return new Success(heroes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisassembleResult disassemble(long userId, long heroId, int count) throws AppException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to disassemble must be positive");
        }
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found: " + heroId));

        int shardsGained = gameConfig.rarity(hero.rarity()).shardsYield() * count;
        int shardsBalance = userCardRepository.disassemble(userId, heroId, count, shardsGained);

        return new DisassembleResult(hero, count, shardsGained, shardsBalance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull CraftResult craft(long userId, @NotNull HeroRarity rarity) throws AppException {
        int craftCost = gameConfig.rarity(rarity).craftCost();
        Hero hero = heroService.getRandomHero(rarity)
                .orElseThrow(() -> new IllegalStateException("No heroes available for rarity " + rarity));

        int shardsBalance = userCardRepository.craft(userId, hero.id(), craftCost);

        return new CraftResult(hero, craftCost, shardsBalance);
    }

    /**
     * Проверяет, истёк ли кулдаун.
     * @return оставшееся время или null, если награда доступна
     */
    private Duration timeUntilNextClaim(LocalDateTime lastDailyAt, LocalDateTime now) {
        if (lastDailyAt == null) return null;
        Duration elapsed = Duration.between(lastDailyAt, now);
        Duration cooldown = gameConfig.dailyCooldown();
        return elapsed.compareTo(cooldown) >= 0 ? null : cooldown.minus(elapsed);
    }
}
