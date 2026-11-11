package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.Cooldown;
import com.avengers.cardgame.service.Success;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

/**
 * Тестовый сервис карточек с заранее заданным результатом ежедневной награды
 * и настраиваемым результатом крафта.
 */
public class FakeCardService implements CardService {

    /** Герои, которые выдаются в ежедневной награде. */
    private final List<Hero> heroes;

    /** Кулдаун ежедневной награды или {@code null}, если награда доступна. */
    private final Duration cooldown;

    /** Количество кусочков, доступное для крафта. */
    private int shards;

    /** Последний идентификатор пользователя, запросившего ежедневную награду. */
    private long lastUserId;

    /** Последний разобранный герой. */
    private long lastDisassembledHeroId;

    /** Последнее разобранное количество карточек. */
    private int lastDisassembledCount;

    /** Последняя запрошенная для крафта редкость. */
    private HeroRarity lastCraftedRarity;

    /**
     * @param heroes герои ежедневной награды
     */
    public FakeCardService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
        this.cooldown = null;
    }

    /**
     * @param cooldown кулдаун ежедневной награды
     */
    public FakeCardService(Duration cooldown) {
        this.heroes = List.of();
        this.cooldown = cooldown;
    }

    @Override
    public DailyRewardResult grantDaily(long userId) {
        lastUserId = userId;
        if (cooldown != null) {
            return new Cooldown(cooldown);
        }
        return new Success(heroes);
    }

    @Override
    public int disassemble(long userId, long heroId, int count)
            throws NotEnoughCardsException {
        Hero hero = heroes.stream().filter(candidate -> candidate.id() == heroId).findFirst()
                .orElseThrow(() -> new NotEnoughCardsException(userId, heroId, count, 0));
        lastDisassembledHeroId = heroId;
        lastDisassembledCount = count;
        return hero.rarity().shardsYield() * count;
    }

    @Override
    public Hero craft(long userId, @NotNull HeroRarity rarity) throws InsufficientShardsException {
        lastCraftedRarity = rarity;
        if (shards < rarity.craftCost()) {
            throw new InsufficientShardsException(rarity.craftCost(), shards);
        }
        shards -= rarity.craftCost();
        return heroes.stream().filter(hero -> hero.rarity() == rarity).findFirst()
                .orElse(new Hero(0, "Crafted " + rarity, rarity, 1, 1, 1, ""));
    }

    /**
     * @return идентификатор последнего пользователя, запросившего награду
     */
    public long lastUserId() {
        return lastUserId;
    }

    /**
     * @return идентификатор последнего разобранного героя
     */
    public long lastDisassembledHeroId() {
        return lastDisassembledHeroId;
    }

    /**
     * @return последнее разобранное количество
     */
    public int lastDisassembledCount() {
        return lastDisassembledCount;
    }

    /**
     * @return последняя запрошенная для крафта редкость
     */
    public HeroRarity lastCraftedRarity() {
        return lastCraftedRarity;
    }

    /**
     * Задаёт количество кусочков, доступное для крафта.
     *
     * @param shards количество кусочков
     */
    public void setShards(int shards) {
        this.shards = shards;
    }
}
