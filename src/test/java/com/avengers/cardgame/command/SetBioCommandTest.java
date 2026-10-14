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
 * Тесты для {@link SetBioCommand}.
 */
class SetBioCommandTest {

    /** Фейковый репозиторий пользователей. */
    private FakeUserRepository userRepository;

    /** Тестируемая команда. */
    private SetBioCommand command;

    /**
     * Создаёт фейковый репозиторий и команду перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        command = new SetBioCommand(new UserServiceImpl(userRepository));
    }

    @Test
    @DisplayName("Обновляет bio при валидном вводе")
    void updatesBioWhenValid() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Tony")));

        Reply reply = command.handle(1L, "Tony", "Genius billionaire");

        Assertions.assertEquals(
                "Вы обновили описание своего профиля на Genius billionaire", reply.text());
        Assertions.assertEquals("Genius billionaire",
                userRepository.findByUserId(1L).orElseThrow().bio().value());
    }

    @Test
    @DisplayName("Возвращает ошибку при невалидном bio")
    void returnsErrorWhenInvalid() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Tony")));

        Reply reply = command.handle(1L, "Tony", "Текст с \u0000 нулевым символом");

        String expected = "Вы ввели некорректное описание профиля. "
                + "Допустимо использовать до 256 символов латиницей или кириллицей "
                + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
                + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";
        Assertions.assertEquals(expected, reply.text());
        Assertions.assertNull(userRepository.findByUserId(1L).orElseThrow().bio());
    }

    @Test
    @DisplayName("Возвращает ошибку при пустом аргументе")
    void returnsErrorWhenEmptyArgument() throws Exception {
        userRepository.save(new User(1L, new PlayerName("Tony")));

        Reply reply = command.handle(1L, "Tony", "");

        String expected = "Вы ввели некорректное описание профиля. "
                + "Допустимо использовать до 256 символов латиницей или кириллицей "
                + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
                + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";
        Assertions.assertEquals(expected, reply.text());
        Assertions.assertNull(userRepository.findByUserId(1L).orElseThrow().bio());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException, если пользователя нет")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> command.handle(1L, "Tony", "some bio"));
    }
}