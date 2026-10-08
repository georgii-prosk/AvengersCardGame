package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;
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

    /**
     * Создаёт фейковый репозиторий и сервис перед каждым тестом.
     */
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

        @Test
        @DisplayName("Регистрирует с пустым именем — разрешено на уровне сервиса")
        void registersWithEmptyName() {
            User created = userService.registerIfAbsent(1L, "");

            Assertions.assertEquals("", created.getDisplayName());
        }

        @Test
        @DisplayName("Регистрирует с кириллическим именем")
        void registersWithCyrillicName() {
            User created = userService.registerIfAbsent(1L, "Нарек");

            Assertions.assertEquals("Нарек", created.getDisplayName());
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
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.getByTelegramId(999L));
        }

        @Test
        @DisplayName("Сообщение об ошибке содержит Telegram ID")
        void errorMessageContainsTelegramId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByTelegramId(42L));

            Assertions.assertTrue(ex.getMessage().contains("42"));
        }

        @Test
        @DisplayName("Исключение содержит правильный Telegram ID")
        void exceptionCarriesTelegramId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByTelegramId(42L));

            Assertions.assertEquals(42L, ex.getTelegramId());
        }

        @Test
        @DisplayName("Исключение является RuntimeException")
        void exceptionIsRuntimeException() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByTelegramId(1L));

            Assertions.assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        @DisplayName("Находит правильного пользователя среди нескольких")
        void findsCorrectUserAmongMany() {
            userRepository.save(new User(1L, "Tony"));
            userRepository.save(new User(2L, "Steve"));
            userRepository.save(new User(3L, "Natasha"));

            Assertions.assertEquals("Steve", userService.getByTelegramId(2L).getDisplayName());
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
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, "Iron Man"));
        }

        @Test
        @DisplayName("Исключение содержит правильный Telegram ID")
        void exceptionCarriesTelegramId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, "Iron Man"));

            Assertions.assertEquals(999L, ex.getTelegramId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
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

        @Test
        @DisplayName("Сохраняет balance при обновлении имени")
        void keepsBalanceUnchanged() {
            User user = new User(1L, "Tony");
            user.setBalance(500);
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals(500,
                    userRepository.findByTelegramId(1L).orElseThrow().getBalance());
        }

        @Test
        @DisplayName("Сохраняет cardCount при обновлении имени")
        void keepsCardCountUnchanged() {
            User user = new User(1L, "Tony");
            user.setCardCount(10);
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals(10,
                    userRepository.findByTelegramId(1L).orElseThrow().getCardCount());
        }

        @Test
        @DisplayName("Обновляет имя на кириллицу — без валидации на уровне сервиса")
        void updatesToCyrillicName() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateDisplayName(1L, "Нарек");

            Assertions.assertEquals("Нарек",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
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
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(999L, "New bio"));
        }

        @Test
        @DisplayName("Исключение содержит правильный Telegram ID")
        void exceptionCarriesTelegramId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateBio(777L, "New bio"));

            Assertions.assertEquals(777L, ex.getTelegramId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(1L, "New bio"));

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
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
        @DisplayName("Сохраняет balance при обновлении bio")
        void keepsBalanceUnchanged() {
            User user = new User(1L, "Tony");
            user.setBalance(500);
            userRepository.save(user);

            userService.updateBio(1L, "New bio");

            Assertions.assertEquals(500,
                    userRepository.findByTelegramId(1L).orElseThrow().getBalance());
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

        @Test
        @DisplayName("Многострочное bio сохраняется корректно")
        void savesMultilineBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Line one\nLine two\nLine three");

            Assertions.assertEquals("Line one\nLine two\nLine three",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
        }

        @Test
        @DisplayName("Длинное bio (256 символов) сохраняется")
        void savesMaxLengthBio() {
            userRepository.save(new User(1L, "Tony"));
            String longBio = "b".repeat(256);

            userService.updateBio(1L, longBio);

            Assertions.assertEquals(longBio,
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