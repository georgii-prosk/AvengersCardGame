package com.avengers.cardgame.fakes;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.Cooldown;
import com.avengers.cardgame.service.CraftResult;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.DisassembleResult;
import com.avengers.cardgame.service.Success;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

/**
 * Фейковый сервис карточек: выдаёт заданных героев и умеет бросать ошибки.
 */
public class FakeCardService implements CardService {

    /** Герои, которые возвращает фейк. */
    private final List<Hero> heroes;

    /** Кулдаун ежедневной награды, {@code null} если награда доступна. */
    private final Duration cooldown;

    /** Идентификатор последнего пользователя, запросившего награду. */
    private long lastUserId;

    /** Ошибка разбора карточек. */
    private AppException disassembleError;

    /** Ошибка крафта. */
    private AppException craftError;

    /** Конфигурация со значениями экономики по умолчанию. */
    private final GameConfig defaultConfig = new TestGameConfig().defaultConfig();

    /**
     * @param heroes герои, которые будет возвращать фейк
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

    /**
     * Задаёт ошибку разбора карточек.
     *
     * @param exception ошибка
     */
    public void disassembleThrows(@NotNull AppException exception) {
        this.disassembleError = exception;
    }

    /**
     * Задаёт ошибку крафта.
     *
     * @param exception ошибка
     */
    public void craftThrows(@NotNull AppException exception) {
        this.craftError = exception;
    }

    @Override
    public DailyRewardResult grantDaily(long userId) {
        lastUserId = userId;
        if (cooldown != null) {
            return new Cooldown(cooldown);
        }
        return new Success(heroes);
    }

    /**
     * @return идентификатор последнего пользователя, запросившего награду
     */
    public long lastUserId() {
        return lastUserId;
    }

    @Override
    public DisassembleResult disassemble(long userId, long heroId, int count) throws AppException {
        if (disassembleError != null) {
            throw disassembleError;
        }
        return new DisassembleResult(firstHero(), count, count, count);
    }

    @Override
    public @NotNull CraftResult craft(long userId, @NotNull HeroRarity rarity) throws AppException {
        if (craftError != null) {
            throw craftError;
        }
        int craftCost = defaultConfig.rarity(rarity).craftCost();
        return new CraftResult(firstHero(), craftCost, 0);
    }

    /**
     * @return первый герой из заданных
     */
    private Hero firstHero() {
        if (heroes.isEmpty()) {
            throw new IllegalStateException("FakeCardService has no heroes");
        }
        return heroes.getFirst();
    }
}
