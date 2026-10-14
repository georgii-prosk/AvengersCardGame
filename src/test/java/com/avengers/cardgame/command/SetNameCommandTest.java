package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link SetNameCommand}.
 */
class SetNameCommandTest {

    /** Фейковый репозиторий пользователей. */
    private FakeUserRepository userRepository;

    /** Тестируемая команда. */
    private SetNameCommand command;

    /**
     * Создаёт фейковый репозиторий и команду перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        command = new SetNameCommand(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Обновляет имя при валидном вводе")
    void updatesNameWhenValid() throws Exception {
        userRepository.save(new User(1L, new PlayerName("OldName")));

        Reply reply = command.handle(1L, "Tony", "Narek");

        Assertions.assertEquals("Вы сменили отображаемое имя на Narek", reply.text());
        Assertions.assertEquals("Narek",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Возвращает ошибку при невалидном имени")
    void returnsErrorWhenInvalid() throws Exception {
        userRepository.save(new User(1L, new PlayerName("OldName")));

        Reply reply = command.handle(1L, "Tony", "bad name");

        Assertions.assertEquals(
                "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.",
                reply.text());
        Assertions.assertEquals("OldName",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Возвращает ошибку при пустом аргументе")
    void returnsErrorWhenEmptyArgument() throws Exception {
        userRepository.save(new User(1L, new PlayerName("OldName")));

        Reply reply = command.handle(1L, "Tony", "");

        Assertions.assertEquals(
                "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.",
                reply.text());
        Assertions.assertEquals("OldName",
                userRepository.findByUserId(1L).orElseThrow().playerName().value());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException, если пользователя нет")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> command.handle(1L, "Tony", "Narek"));
    }
}