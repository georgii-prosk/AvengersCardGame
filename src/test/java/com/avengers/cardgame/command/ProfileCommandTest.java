package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ProfileCommand}.
 */
class ProfileCommandTest {

    /** Фейковый репозиторий пользователей. */
    private FakeUserRepository userRepository;

    /** Тестируемая команда. */
    private ProfileCommand command;

    /**
     * Создаёт фейковый репозиторий и команду перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        command = new ProfileCommand(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Возвращает полные данные профиля")
    void returnsFullProfile() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Tony"), new Bio("Genius"),
                100, 5, null, null));

        Reply reply = command.handle(1L, "Tony", "");

        String expected = """
                Имя: Tony
                Описание: Genius
                Твой баланс: 100 щитов
                В твоей коллекции: 5 карточек
                """;
        Assertions.assertEquals(expected, reply.text());
    }

    @Test
    @DisplayName("Показывает «не задано» для пустого bio")
    void showsNotSetForEmptyBio() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Tony")));

        Reply reply = command.handle(1L, "Tony", "");

        String expected = """
                Имя: Tony
                Описание: не задано
                Твой баланс: 0 щитов
                В твоей коллекции: 0 карточек
                """;
        Assertions.assertEquals(expected, reply.text());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException, если пользователя нет")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> command.handle(1L, "Tony", ""));
    }
}