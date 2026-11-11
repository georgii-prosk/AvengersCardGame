package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.Cooldown;
import com.avengers.cardgame.service.Success;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Реализация {@link CardService}.
 */
public class CardServiceImpl implements CardService {
    /** Минимальное количество карточек, выдаваемых за одну ежедневную награду. */
    private static final int MIN_DAILY_CARDS = 1;

    /** Максимальное количество карточек, выдаваемых за одну ежедневную награду. */
    private static final int MAX_DAILY_CARDS = 3;

    /** Логгер. */
    private static final Logger logger = LoggerFactory.getLogger(CardServiceImpl.class);

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
    /** Часы для отслеживания времени кулдауна. */
    private final Clock clock;
    /** Фабрика соединений для транзакций. */
    private final ConnectionFactory connectionFactory;
    /** Репозиторий героев. */
    private final HeroRepository heroRepository;
    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /**
     * @param userService        сервис пользователей
     * @param userCardRepository репозиторий карточек
     * @param heroService        сервис героев
     * @param gameConfig         игровая конфигурация
     * @param random             генератор случайных чисел
     * @param clock              часы
     * @param connectionFactory  фабрика соединений
     * @param heroRepository     репозиторий героев
     * @param userRepository     репозиторий пользователей
     */
    public CardServiceImpl(UserService userService, UserCardRepository userCardRepository,
                           HeroService heroService, GameConfig gameConfig, Random random, Clock clock,
                           ConnectionFactory connectionFactory, HeroRepository heroRepository,
                           UserRepository userRepository) {
        this.userService = userService;
        this.userCardRepository = userCardRepository;
        this.heroService = heroService;
        this.gameConfig = gameConfig;
        this.random = random;
        this.clock = clock;
        this.connectionFactory = connectionFactory;
        this.heroRepository = heroRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DailyRewardResult grantDaily(long userId) throws UserNotFoundException {
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
    public int disassemble(long userId, long heroId, int count)
            throws UserNotFoundException, NotEnoughCardsException {
        if (count <= 0) throw new IllegalArgumentException("Count must be positive");
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown hero: " + heroId));
        userService.getByUserId(userId);
        int available = availableCards(userId, heroId);

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);

            for (int index = 0; index < count; index++) {
                boolean cardRemoved = userCardRepository.removeCard(connection, userId, heroId);
                if (!cardRemoved) {
                    throw new NotEnoughCardsException(userId, heroId, count, available);
                }
            }

            int shards = Math.multiplyExact(hero.rarity().shardsYield(), count);
            if (!userRepository.addShards(connection, userId, shards)) {
                throw new UserNotFoundException(userId);
            }

            connection.commit();
            return shards;
        } catch (AppException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Error disassembling cards", exception);
        } finally {
            close(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Hero craft(long userId, @NotNull HeroRarity rarity)
            throws UserNotFoundException, InsufficientShardsException {
        Objects.requireNonNull(rarity, "Rarity must not be null");
        User user = userService.getByUserId(userId);
        int cost = rarity.craftCost();

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);

            if (!userRepository.spendShards(connection, userId, cost)) {
                throw new InsufficientShardsException(cost, user.shards());
            }

            Hero hero = heroService.getRandomHero(rarity)
                    .orElseThrow(() -> new IllegalStateException("No heroes of rarity " + rarity));
            userCardRepository.addCard(connection, userId, hero.id());

            connection.commit();
            return hero;
        } catch (AppException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Error crafting card", exception);
        } finally {
            close(connection);
        }
    }

    /**
     * Считает количество копий карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    private int availableCards(long userId, long heroId) {
        return userCardRepository.findByUserId(userId).stream()
                .filter(card -> card.heroId() == heroId)
                .mapToInt(UserCard::count)
                .sum();
    }

    /**
     * Проверяет, истёк ли кулдаун.
     *
     * @param lastDailyAt время последней выдачи
     * @param now         текущее время
     * @return оставшееся время или {@code null}, если награда доступна
     */
    private Duration timeUntilNextClaim(LocalDateTime lastDailyAt, LocalDateTime now) {
        if (lastDailyAt == null) return null;
        Duration elapsed = Duration.between(lastDailyAt, now);
        Duration cooldown = gameConfig.dailyCooldown();
        return elapsed.compareTo(cooldown) >= 0 ? null : cooldown.minus(elapsed);
    }

    /**
     * Откатывает транзакцию, логируя ошибку отката.
     *
     * @param connection соединение или {@code null}
     */
    private void rollback(Connection connection) {
        if (connection == null) return;
        try {
            connection.rollback();
        } catch (SQLException exception) {
            logger.error("Failed to rollback transaction", exception);
        }
    }

    /**
     * Закрывает соединение, логируя ошибку закрытия.
     *
     * @param connection соединение или {@code null}
     */
    private void close(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException exception) {
            logger.error("Failed to close connection", exception);
        }
    }
}
