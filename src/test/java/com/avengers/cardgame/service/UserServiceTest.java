package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link UserServiceImpl}.
 */
class UserServiceTest {

    /** Фейковый репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Тестируемый сервис. */
    private UserServiceImpl userService;

    /**
     * Создаёт фейковый репозиторий и сервис перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    /**
     * Тесты для {@link UserServiceImpl#registerIfAbsent(long, PlayerName)}.
     */
    @Nested
    @DisplayName("registerIfAbsent")
    class RegisterIfAbsentTest {

        @Test
        @DisplayName("Регистрирует нового пользователя")
        void registersNewUser() throws Exception {
            User created = userService.registerIfAbsent(1L, new PlayerName("Tony"));

            Assertions.assertNotNull(created);
            Assertions.assertEquals(1L, created.userId());
            Assertions.assertEquals("Tony", created.playerName().value());
            Assertions.assertTrue(userRepository.findByUserId(1L).isPresent());
        }

        @Test
        @DisplayName("Возвращает существующего пользователя без перезаписи")
        void returnsExistingUserWithoutOverwrite() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Original")));

            User result = userService.registerIfAbsent(1L, new PlayerName("Tony"));

            Assertions.assertEquals("Original", result.playerName().value());
            Assertions.assertEquals("Original",
                    userRepository.findByUserId(1L).orElseThrow().playerName().value());
        }

        @Test
        @DisplayName("Создаёт двух разных пользователей")
        void createsTwoDifferentUsers() throws Exception {
            userService.registerIfAbsent(1L, new PlayerName("Tony"));
            userService.registerIfAbsent(2L, new PlayerName("Steve"));

            Assertions.assertEquals("Tony",
                    userRepository.findByUserId(1L).orElseThrow().playerName().value());
            Assertions.assertEquals("Steve",
                    userRepository.findByUserId(2L).orElseThrow().playerName().value());
        }

        @Test
        @DisplayName("Не увеличивает количество пользователей при повторной регистрации")
        void doesNotIncreaseCountOnSecondRegister() throws Exception {
            userService.registerIfAbsent(1L, new PlayerName("Tony"));
            userService.registerIfAbsent(1L, new PlayerName("Tony"));

            Assertions.assertEquals(1, userRepository.size());
        }
    }

    /**
     * Тесты для {@link UserServiceImpl#getByUserId(long)}.
     */
    @Nested
    @DisplayName("getByUserId")
    class GetByUserIdTest {

        @Test
        @DisplayName("Возвращает сохранённого пользователя")
        void returnsSavedUser() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            User found = userService.getByUserId(1L);

            Assertions.assertEquals(1L, found.userId());
            Assertions.assertEquals("Tony", found.playerName().value());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.getByUserId(999L));
        }

        @Test
        @DisplayName("Сообщение об ошибке содержит идентификатор")
        void errorMessageContainsUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByUserId(42L));

            Assertions.assertTrue(ex.getMessage().contains("42"));
        }

        @Test
        @DisplayName("Исключение содержит правильный идентификатор")
        void exceptionCarriesUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByUserId(42L));

            Assertions.assertEquals(42L, ex.userId());
        }

        @Test
        @DisplayName("Находит правильного пользователя среди нескольких")
        void findsCorrectUserAmongMany() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));
            userRepository.save(new User(2L, new PlayerName("Steve")));
            userRepository.save(new User(3L, new PlayerName("Natasha")));

            Assertions.assertEquals("Steve",
                    userService.getByUserId(2L).playerName().value());
        }
    }

    /**
     * Тесты для {@link UserServiceImpl#updateDisplayName(long, PlayerName)}.
     */
    @Nested
    @DisplayName("updateDisplayName")
    class UpdateDisplayNameTest {

        @Test
        @DisplayName("Обновляет имя существующего пользователя")
        void updatesExistingUser() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            userService.updateDisplayName(1L, new PlayerName("IronMan"));

            Assertions.assertEquals("IronMan",
                    userRepository.findByUserId(1L).orElseThrow().playerName().value());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, new PlayerName("IronMan")));
        }

        @Test
        @DisplayName("Исключение содержит правильный идентификатор")
        void exceptionCarriesUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, new PlayerName("IronMan")));

            Assertions.assertEquals(999L, ex.userId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateDisplayName(1L, new PlayerName("IronMan")));

            Assertions.assertTrue(userRepository.findByUserId(1L).isEmpty());
        }

        @Test
        @DisplayName("Сохраняет bio при обновлении имени")
        void keepsBioUnchanged() throws Exception {
            User user = new User(1L, new PlayerName("Tony"), new Bio("Genius"), 0, 0, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, new PlayerName("IronMan"));

            Assertions.assertEquals("Genius",
                    userRepository.findByUserId(1L).orElseThrow().bio().value());
        }

        @Test
        @DisplayName("Сохраняет balance при обновлении имени")
        void keepsBalanceUnchanged() throws Exception {
            User user = new User(1L, new PlayerName("Tony"), null, 500, 0, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, new PlayerName("IronMan"));

            Assertions.assertEquals(500,
                    userRepository.findByUserId(1L).orElseThrow().balance());
        }

        @Test
        @DisplayName("Сохраняет cardCount при обновлении имени")
        void keepsCardCountUnchanged() throws Exception {
            User user = new User(1L, new PlayerName("Tony"), null, 0, 10, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, new PlayerName("IronMan"));

            Assertions.assertEquals(10,
                    userRepository.findByUserId(1L).orElseThrow().cardCount());
        }
    }

    /**
     * Тесты для {@link UserServiceImpl#updateBio(long, Bio)}.
     */
    @Nested
    @DisplayName("updateBio")
    class UpdateBioTest {

        @Test
        @DisplayName("Обновляет bio существующего пользователя")
        void updatesExistingUserBio() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            userService.updateBio(1L, new Bio("Genius billionaire"));

            Assertions.assertEquals("Genius billionaire",
                    userRepository.findByUserId(1L).orElseThrow().bio().value());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(999L, new Bio("New bio")));
        }

        @Test
        @DisplayName("Исключение содержит правильный идентификатор")
        void exceptionCarriesUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateBio(777L, new Bio("New bio")));

            Assertions.assertEquals(777L, ex.userId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(1L, new Bio("New bio")));

            Assertions.assertTrue(userRepository.findByUserId(1L).isEmpty());
        }

        @Test
        @DisplayName("Сохраняет имя при обновлении bio")
        void keepsDisplayNameUnchanged() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            userService.updateBio(1L, new Bio("New bio"));

            Assertions.assertEquals("Tony",
                    userRepository.findByUserId(1L).orElseThrow().playerName().value());
        }

        @Test
        @DisplayName("Сохраняет balance при обновлении bio")
        void keepsBalanceUnchanged() throws Exception {
            User user = new User(1L, new PlayerName("Tony"), new Bio("Genius"), 500, 0, null, null);
            userRepository.save(user);

            userService.updateBio(1L, new Bio("New bio"));

            Assertions.assertEquals(500,
                    userRepository.findByUserId(1L).orElseThrow().balance());
        }

        @Test
        @DisplayName("Сохраняет кириллицу и эмодзи")
        void savesCyrillicAndEmoji() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            userService.updateBio(1L, new Bio("Привет! 🦸"));

            Assertions.assertEquals("Привет! 🦸",
                    userRepository.findByUserId(1L).orElseThrow().bio().value());
        }

        @Test
        @DisplayName("Многострочное bio сохраняется корректно")
        void savesMultilineBio() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));

            userService.updateBio(1L, new Bio("Line one\nLine two\nLine three"));

            Assertions.assertEquals("Line one\nLine two\nLine three",
                    userRepository.findByUserId(1L).orElseThrow().bio().value());
        }

        @Test
        @DisplayName("Длинное bio (256 символов) сохраняется")
        void savesMaxLengthBio() throws Exception {
            userRepository.save(new User(1L, new PlayerName("Tony")));
            String longBioText = "b".repeat(256);

            userService.updateBio(1L, new Bio(longBioText));

            Assertions.assertEquals(longBioText,
                    userRepository.findByUserId(1L).orElseThrow().bio().value());
        }
    }
}