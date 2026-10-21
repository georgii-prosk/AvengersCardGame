package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.service.UserService;

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

    /** Минимальное количество карточек за одну выдачу. */
    private static final int MIN_DAILY_CARDS = 1;

    /** Максимальное количество карточек за одну выдачу. */
    private static final int MAX_DAILY_CARDS = 3;

    /** Сервис пользователей. */
    private final UserService userService;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Сервис героев. */
    private final HeroService heroService;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Источник случайности. */
    private final Random random;

    /** Часы, по которым отсчитывается кулдаун. */
    private final Clock clock;

    /**
     * @param userService        сервис пользователей
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroService        сервис героев
     * @param gameConfig         игровая конфигурация
     * @param random             источник случайности
     * @param clock              часы
     */
    public CardServiceImpl(
            UserService userService,
            UserCardRepository userCardRepository,
            HeroService heroService,
            GameConfig gameConfig,
            Random random,
            Clock clock
    ) {
        this.userService = userService;
        this.userCardRepository = userCardRepository;
        this.heroService = heroService;
        this.gameConfig = gameConfig;
        this.random = random;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Метод синхронизирован, чтобы два одновременных {@code /daily} от одного
     * игрока не прошли проверку кулдауна оба и не выдали награду дважды.
     */
    @Override
    public synchronized List<Hero> grantDaily(long userId)
            throws UserNotFoundException, DailyCooldownException {
        User user = userService.getByUserId(userId);
        LocalDateTime now = LocalDateTime.now(clock);

        checkCooldown(user.lastDailyAt(), now);

        int cardsCount = MIN_DAILY_CARDS + random.nextInt(MAX_DAILY_CARDS - MIN_DAILY_CARDS + 1);
        List<Hero> granted = new ArrayList<>(cardsCount);
        for (int i = 0; i < cardsCount; i++) {
            Hero hero = heroService.getRandomHero();
            userCardRepository.addCard(userId, hero.id());
            granted.add(hero);
        }

        userService.updateLastDailyAt(userId, now);
        return granted;
    }

    /**
     * Проверяет, что с прошлой выдачи прошло не меньше кулдауна.
     *
     * @param lastDailyAt время прошлой выдачи, {@code null} если выдач ещё не было
     * @param now         текущее время
     * @throws DailyCooldownException если кулдаун ещё не истёк
     */
    private void checkCooldown(LocalDateTime lastDailyAt, LocalDateTime now)
            throws DailyCooldownException {
        if (lastDailyAt == null) {
            return;
        }
        Duration elapsed = Duration.between(lastDailyAt, now);
        Duration cooldown = gameConfig.dailyCooldown();
        if (elapsed.compareTo(cooldown) < 0) {
            throw new DailyCooldownException(cooldown.minus(elapsed));
        }
    }
}
