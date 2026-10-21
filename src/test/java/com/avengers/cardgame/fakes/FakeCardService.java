package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.CardService;

import java.util.List;

/**
 * Сервис карточек для тестов: возвращает заранее заданный результат.
 */
public class FakeCardService implements CardService {

    /** Герои, которых «выдаём»; используются, если {@link #failure} не задан. */
    private final List<Hero> heroes;

    /** Ошибка кулдауна, которую нужно бросить; {@code null} — выдача проходит успешно. */
    private final DailyCooldownException failure;

    /** Идентификатор пользователя из последнего вызова. */
    private long lastUserId;

    /**
     * Сервис, успешно выдающий указанных героев.
     *
     * @param heroes герои
     */
    public FakeCardService(List<Hero> heroes) {
        this.heroes = heroes;
        this.failure = null;
    }

    /**
     * Сервис, всегда отвечающий кулдауном.
     *
     * @param failure бросаемая ошибка
     */
    public FakeCardService(DailyCooldownException failure) {
        this.heroes = List.of();
        this.failure = failure;
    }

    @Override
    public List<Hero> grantDaily(long userId) throws DailyCooldownException {
        lastUserId = userId;
        if (failure != null) {
            throw failure;
        }
        return heroes;
    }

    /**
     * @return идентификатор пользователя из последнего вызова
     */
    public long lastUserId() {
        return lastUserId;
    }
}
