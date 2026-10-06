package com.avengers.cardgame.service;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для {@link UserService}.
 * <p>
 * Используется фейковый репозиторий в памяти — без Mockito и без Docker.
 * Тестируется вся бизнес-логика сервиса: регистрация, поиск, обновления.
 */
class UserServiceTest {

    /** Фейковый репозиторий пользователей в памяти. */
    private FakeUserRepository userRepository;

    /** Тестируемый сервис. */
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
    }

    /**
     * Тесты для {@link UserService#registerIfAbsent(long, String)}.
     */
    @Nested
    @DisplayName("registerIfAbsent")
    class RegisterIfAbsentTest {

        @Test
        @DisplayName("Регистрирует нового пользователя")
        void registersNewUser() {
            User created = userService.registerIfAbsent(1L, "Tony");

            Assertions.assertNotNull(created);
            Assertions.assertEquals(1L, created.getTelegramId());
            Assertions.assertEquals("Tony", created.getDisplayName());
            Assertions.assertTrue(userRepository.findByTelegramId(1L).isPresent());
        }

        @Test
        @DisplayName("Возвращает существующего пользователя без перезаписи")
        void returnsExistingUserWithoutOverwrite() {
            userRepository.save(new User(1L, "Original"));

            User result = userService.registerIfAbsent(1L, "Tony");

            Assertions.assertEquals("Original", result.getDisplayName());
            Assertions.assertEquals("Original",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }

        @Test
        @DisplayName("Создаёт двух разных пользователей")
        void createsTwoDifferentUsers() {
            userService.registerIfAbsent(1L, "Tony");
            userService.registerIfAbsent(2L, "Steve");

            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
            Assertions.assertEquals("Steve",
                    userRepository.findByTelegramId(2L).orElseThrow().getDisplayName());
        }

        @Test
        @DisplayName("Не увеличивает количество пользователей при повторной регистрации")
        void doesNotIncreaseCountOnSecondRegister() {
            userService.registerIfAbsent(1L, "Tony");
            userService.registerIfAbsent(1L, "Tony");

            Assertions.assertEquals(1, userRepository.size());
        }
    }

    /**
     * Тесты для {@link UserService#getByTelegramId(long)}.
     */
    @Nested
    @DisplayName("getByTelegramId")
    class GetByTelegramIdTest {

        @Test
        @DisplayName("Возвращает сохранённого пользователя")
        void returnsSavedUser() {
            userRepository.save(new User(1L, "Tony"));

            User found = userService.getByTelegramId(1L);

            Assertions.assertEquals(1L, found.getTelegramId());
            Assertions.assertEquals("Tony", found.getDisplayName());
        }

        @Test
        @DisplayName("Бросает RuntimeException, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> userService.getByTelegramId(999L));
        }

        @Test
        @DisplayName("Сообщение об ошибке содержит Telegram ID")
        void errorMessageContainsTelegramId() {
            RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                    () -> userService.getByTelegramId(42L));

            Assertions.assertTrue(ex.getMessage().contains("42"));
        }
    }

    /**
     * Тесты для {@link UserService#updateDisplayName(long, String)}.
     */
    @Nested
    @DisplayName("updateDisplayName")
    class UpdateDisplayNameTest {

        @Test
        @DisplayName("Обновляет имя существующего пользователя")
        void updatesExistingUser() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals("Iron Man",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }

        @Test
        @DisplayName("Бросает исключение, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> userService.updateDisplayName(999L, "Iron Man"));
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> userService.updateDisplayName(1L, "Iron Man"));

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
        }

        @Test
        @DisplayName("Сохраняет bio при обновлении имени")
        void keepsBioUnchanged() {
            User user = new User(1L, "Tony");
            user.setBio("Genius");
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals("Genius",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
        }
    }

    /**
     * Тесты для {@link UserService#updateBio(long, String)}.
     */
    @Nested
    @DisplayName("updateBio")
    class UpdateBioTest {

        @Test
        @DisplayName("Обновляет bio существующего пользователя")
        void updatesExistingUserBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Genius, billionaire, playboy, philanthropist");

            Assertions.assertEquals("Genius, billionaire, playboy, philanthropist",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
        }

        @Test
        @DisplayName("Бросает исключение, если пользователя нет")
        void throwsWhenUserNotFound() {
            Assertions.assertThrows(RuntimeException.class,
                    () -> userService.updateBio(999L, "New bio"));
        }

        @Test
        @DisplayName("Сохраняет имя при обновлении bio")
        void keepsDisplayNameUnchanged() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "New bio");

            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }

        @Test
        @DisplayName("Сохраняет кириллицу и эмодзи")
        void savesCyrillicAndEmoji() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Привет! 🦸‍♂️");

            Assertions.assertEquals("Привет! 🦸‍♂️",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
        }

        @Test
        @DisplayName("Пустое bio сохраняется как пустая строка")
        void savesEmptyBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "");

            Assertions.assertEquals("",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
        }
    }

    /**
     * Фейковый репозиторий пользователей в памяти.
     * <p>
     * Полноценная реализация {@link UserRepository} без обращения к БД.
     * Не мок — все методы реально выполняются над {@link HashMap}.
     */
    private class FakeUserRepository implements UserRepository {

        /** Хранилище пользователей по Telegram ID. */
        private final Map<Long, User> storage = new HashMap<>();

        @Override
        public Optional<User> findByTelegramId(long telegramId) {
            return Optional.ofNullable(storage.get(telegramId));
        }

        @Override
        public void save(User user) {
            storage.put(user.getTelegramId(), user);
        }

        @Override
        public void update(User user) {
            storage.put(user.getTelegramId(), user);
        }

        /**
         * Возвращает количество сохранённых пользователей.
         * Используется в тестах для проверки, что запись действительно произошла.
         *
         * @return количество пользователей в хранилище
         */
        int size() {
            return storage.size();
        }
    }
}