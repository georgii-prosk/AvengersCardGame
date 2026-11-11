package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.UserCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Тесты для {@link InMemoryUserCardRepository}.
 */
class InMemoryUserCardRepositoryTest {

    /** Идентификатор пользователя для тестов. */
    private static final long USER_ID = 1L;

    /** Идентификатор героя для тестов. */
    private static final long HERO_ID = 2L;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserCardRepository();
    }

    /**
     * Проверяет, что повторное добавление увеличивает количество.
     */
    @Test
    @DisplayName("Суммирует повторные добавления")
    void sumsRepeatedAdditions() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        List<UserCard> cards = repository.findByUserId(USER_ID);

        Assertions.assertEquals(1, cards.size());
        Assertions.assertEquals(3, cards.getFirst().count());
        Assertions.assertEquals(3, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет частичное удаление карточек.
     */
    @Test
    @DisplayName("Удаляет часть карточек")
    void removesPartOfCards() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 2);

        Assertions.assertTrue(removed);
        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет, что при удалении всех карточек запись исчезает.
     */
    @Test
    @DisplayName("Удаляет запись при полном удалении")
    void removesEntryWhenAllCardsRemoved() {
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 1);

        Assertions.assertTrue(removed);
        Assertions.assertTrue(repository.findByUserId(USER_ID).isEmpty());
    }

    /**
     * Проверяет, что нельзя удалить больше карточек, чем есть.
     */
    @Test
    @DisplayName("Не удаляет больше, чем есть")
    void doesNotRemoveMoreThanAvailable() {
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 2);

        Assertions.assertFalse(removed);
        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет, что удаление отсутствующей карточки возвращает {@code false}.
     */
    @Test
    @DisplayName("Не удаляет отсутствующую карточку")
    void doesNotRemoveMissingCard() {
        Assertions.assertFalse(repository.removeCard(USER_ID, HERO_ID, 1));
    }

    /**
     * Проверяет, что неположительное количество для удаления отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительном количестве")
    void throwsOnNonPositiveCount() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.removeCard(USER_ID, HERO_ID, 0));
    }

    /**
     * Проверяет, что карточки разных пользователей не смешиваются.
     */
    @Test
    @DisplayName("Разделяет карточки пользователей")
    void separatesUsersCards() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(2L, HERO_ID);

        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
        Assertions.assertEquals(1, repository.countByUserId(2L));
    }

    /**
     * Проверяет, что подсчёт ведётся по конкретной карточке.
     */
    @Test
    @DisplayName("Считает копии конкретной карточки")
    void countsCardsByHeroId() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, 3L);

        Assertions.assertEquals(2, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(USER_ID, 3L));
    }

    /**
     * Проверяет, что для отсутствующей карточки возвращается ноль.
     */
    @Test
    @DisplayName("Возвращает ноль для отсутствующей карточки")
    void returnsZeroForMissingCard() {
        repository.addCard(USER_ID, HERO_ID);

        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(USER_ID, 999L));
        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(999L, HERO_ID));
    }
}
