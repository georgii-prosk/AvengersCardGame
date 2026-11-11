package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.UserCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Тесты для {@link InMemoryUserCardRepository}: добавление и удаление карточек.
 */
class InMemoryUserCardRepositoryTest {

    /** Идентификатор пользователя, используемый в тестах. */
    private static final long USER_ID = 1L;

    /** Идентификатор героя, используемый в тестах. */
    private static final long HERO_ID = 7L;

    /** Фейковый репозиторий. */
    private InMemoryUserCardRepository repository;

    /** Фейковое соединение. */
    private FakeConnection connection;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserCardRepository();
        connection = new FakeConnection();
    }

    @Test
    @DisplayName("addCard добавляет карточку")
    void addCardAddsCard() {
        repository.addCard(USER_ID, HERO_ID);

        Assertions.assertEquals(1, repository.countOf(USER_ID, HERO_ID));
        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Повторный addCard увеличивает количество")
    void addCardIncrementsCount() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(connection, USER_ID, HERO_ID);

        Assertions.assertEquals(2, repository.countOf(USER_ID, HERO_ID));
    }

    @Test
    @DisplayName("removeCard уменьшает количество и возвращает true")
    void removeCardDecrementsCount() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(connection, USER_ID, HERO_ID);

        Assertions.assertTrue(removed);
        Assertions.assertEquals(1, repository.countOf(USER_ID, HERO_ID));
    }

    @Test
    @DisplayName("removeCard удаляет опустевшую строку")
    void removeCardDeletesEmptyRow() {
        repository.addCard(USER_ID, HERO_ID);

        Assertions.assertTrue(repository.removeCard(connection, USER_ID, HERO_ID));
        Assertions.assertTrue(repository.findByUserId(USER_ID).isEmpty());
        Assertions.assertEquals(0, repository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("removeCard возвращает false, если карточки нет")
    void removeCardReturnsFalseForMissingCard() {
        Assertions.assertFalse(repository.removeCard(connection, USER_ID, HERO_ID));
    }

    @Test
    @DisplayName("removeCard возвращает false для чужой карточки")
    void removeCardReturnsFalseForAnotherUser() {
        repository.addCard(USER_ID, HERO_ID);

        Assertions.assertFalse(repository.removeCard(connection, USER_ID + 1, HERO_ID));
        Assertions.assertEquals(1, repository.countOf(USER_ID, HERO_ID));
    }

    @Test
    @DisplayName("findByUserId возвращает актуальные количества")
    void findByUserIdReturnsCurrentCounts() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID + 1);

        List<UserCard> cards = repository.findByUserId(USER_ID);

        Assertions.assertEquals(2, cards.size());
        Assertions.assertTrue(cards.stream()
                .anyMatch(card -> card.heroId() == HERO_ID && card.count() == 2));
        Assertions.assertTrue(cards.stream()
                .anyMatch(card -> card.heroId() == HERO_ID + 1 && card.count() == 1));
    }

    @Test
    @DisplayName("countByUserId суммирует количество всех карточек")
    void countByUserIdSumsAllCards() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID + 1);

        Assertions.assertEquals(3, repository.countByUserId(USER_ID));
    }
}
