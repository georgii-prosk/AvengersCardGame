package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.transaction.TransactionManager;

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

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

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

    /** Менеджер транзакций. */
    private final TransactionManager transactionManager;

    /**
     * @param userRepository     репозиторий пользователей
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroService        сервис героев
     * @param gameConfig         игровая конфигурация
     * @param random             источник случайности
     * @param clock              часы
     * @param transactionManager менеджер транзакций
     */
    public CardServiceImpl(
            UserRepository userRepository,
            UserCardRepository userCardRepository,
            HeroService heroService,
            GameConfig gameConfig,
            Random random,
            Clock clock,
            TransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.userCardRepository = userCardRepository;
        this.heroService = heroService;
        this.gameConfig = gameConfig;
        this.random = random;
        this.clock = clock;
        this.transactionManager = transactionManager;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Работает в транзакции с блокировкой строки пользователя
     * ({@code SELECT ... FOR UPDATE}): параллельные вызовы для одного и того же
     * пользователя выполняются последовательно, и второй вызов видит уже
     * обновлённое {@code lastDailyAt} и получает {@link DailyCooldownException}.
     * Параллельные вызовы для разных пользователей друг другу не мешают.
     */
    @Override
    public List<Hero> grantDaily(long userId) throws UserNotFoundException, DailyCooldownException {
        try {
            return transactionManager.inTransaction(conn -> {
                User user = userRepository.findByUserIdForUpdate(conn, userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));
                LocalDateTime now = LocalDateTime.now(clock);

                checkCooldown(user.lastDailyAt(), now);

                int cardsCount = MIN_DAILY_CARDS + random.nextInt(MAX_DAILY_CARDS - MIN_DAILY_CARDS + 1);
                List<Hero> granted = new ArrayList<>(cardsCount);
                for (int i = 0; i < cardsCount; i++) {
                    Hero hero = heroService.getRandomHero();
                    userCardRepository.addCard(conn, userId, hero.id());
                    granted.add(hero);
                }

                userRepository.updateLastDailyAt(conn, userId, now);
                return granted;
            });
        } catch (UserNotFoundException | DailyCooldownException e) {
            throw e;
        } catch (AppException e) {
            // Сюда попадать не должны: лямбда бросает только два типа выше.
            throw new RuntimeException("Unexpected domain error in grantDaily", e);
        }
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