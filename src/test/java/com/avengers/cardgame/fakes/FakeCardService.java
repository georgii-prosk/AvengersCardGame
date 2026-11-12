package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CraftResult;
import com.avengers.cardgame.service.DisassembleResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Фейковый сервис карточек: выдаёт заданных героев и умеет бросать ошибки.
 */
public class FakeCardService implements CardService {

    /** Герои, которые возвращает фейк. */
    private final List<Hero> heroes;

    /** Ошибка разбора карточек. */
    private AppException disassembleError;

    /** Ошибка крафта. */
    private AppException craftError;

    /**
     * @param heroes герои, которые будет возвращать фейк
     */
    public FakeCardService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
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
        return new CraftResult(firstHero(), rarity.craftCost(), 0);
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
