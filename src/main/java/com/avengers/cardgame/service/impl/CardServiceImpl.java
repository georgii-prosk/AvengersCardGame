package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CraftResult;
import com.avengers.cardgame.service.DisassembleResult;
import com.avengers.cardgame.service.HeroService;
import org.jetbrains.annotations.NotNull;

/**
 * Реализация {@link CardService}.
 */
public class CardServiceImpl implements CardService {

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Сервис героев. */
    private final HeroService heroService;

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param userRepository     репозиторий пользователей
     * @param userCardRepository репозиторий карточек
     * @param heroRepository     репозиторий героев
     * @param heroService        сервис героев
     * @param connectionFactory  фабрика соединений
     */
    public CardServiceImpl(UserRepository userRepository,
                           UserCardRepository userCardRepository, HeroRepository heroRepository,
                           HeroService heroService, ConnectionFactory connectionFactory) {
        this.userRepository = userRepository;
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
        this.heroService = heroService;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public DisassembleResult disassemble(long userId, long heroId, int count) throws AppException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to disassemble must be positive");
        }
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found: " + heroId));

        int available = userCardRepository.countByUserIdAndHeroId(userId, heroId);
        int shardsGained = hero.rarity().shardsYield() * count;

        JdbcTransactions.execute(connectionFactory, connection -> {
            if (!userCardRepository.removeCard(connection, userId, heroId, count)) {
                throw new NotEnoughCardsException(heroId, count, available);
            }
            userRepository.addShards(connection, userId, shardsGained);
            return null;
        });

        int shardsBalance = userRepository.findByUserId(userId)
                .map(User::shards)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return new DisassembleResult(hero, count, shardsGained, shardsBalance);
    }

    @Override
    public @NotNull CraftResult craft(long userId, @NotNull HeroRarity rarity) throws AppException {
        int craftCost = rarity.craftCost();

        return JdbcTransactions.execute(connectionFactory, connection -> {
            User user = userRepository.findByUserId(connection, userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
            int available = user.shards();
            if (available < craftCost) {
                throw new InsufficientShardsException(craftCost, available);
            }
            Hero hero = heroService.getRandomHero(rarity)
                    .orElseThrow(() -> new IllegalStateException(
                            "No heroes available for rarity " + rarity));
            if (!userRepository.spendShards(connection, userId, craftCost)) {
                throw new InsufficientShardsException(craftCost, available);
            }
            userCardRepository.addCard(connection, userId, hero.id());
            return new CraftResult(hero, craftCost, available - craftCost);
        });
    }
}
