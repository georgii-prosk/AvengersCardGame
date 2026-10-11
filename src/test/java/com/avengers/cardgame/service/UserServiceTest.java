package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для {@link UserService}.
 * <p>
 * Используется фейковый репозиторий в памяти — без Mockito и без Docker.
 */
class UserServiceTest {

    private FakeUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
    }

    @Nested
    @DisplayName("registerIfAbsent")
    class RegisterIfAbsentTest {

        @Test
        @DisplayName("Регистрирует нового пользователя")
        void registersNewUser() {
            User created = userService.registerIfAbsent(1L, "Tony");

            Assertions.assertNotNull(created);
            Assertions.assertEquals(1L, created.userId());
            Assertions.assertEquals("Tony", created.displayName());
            Assertions.assertTrue(userRepository.findByUserId(1L).isPresent());
        }

        @Test
        @DisplayName("Возвращает существующего пользователя без перезаписи")
        void returnsExistingUserWithoutOverwrite() {
            userRepository.save(new User(1L, "Original"));

            User result = userService.registerIfAbsent(1L, "Tony");

            Assertions.assertEquals("Original", result.displayName());
            Assertions.assertEquals("Original",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
        }

        @Test
        @DisplayName("Создаёт двух разных пользователей")
        void createsTwoDifferentUsers() {
            userService.registerIfAbsent(1L, "Tony");
            userService.registerIfAbsent(2L, "Steve");

            Assertions.assertEquals("Tony",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
            Assertions.assertEquals("Steve",
                    userRepository.findByUserId(2L).orElseThrow().displayName());
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

            Assertions.assertEquals("", created.displayName());
        }

        @Test
        @DisplayName("Регистрирует с кириллическим именем")
        void registersWithCyrillicName() {
            User created = userService.registerIfAbsent(1L, "Нарек");

            Assertions.assertEquals("Нарек", created.displayName());
        }
    }

    @Nested
    @DisplayName("getByUserId")
    class GetByUserIdTest {

        @Test
        @DisplayName("Возвращает сохранённого пользователя")
        void returnsSavedUser() {
            userRepository.save(new User(1L, "Tony"));

            User found = userService.getByUserId(1L);

            Assertions.assertEquals(1L, found.userId());
            Assertions.assertEquals("Tony", found.displayName());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
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
        @DisplayName("Исключение является RuntimeException")
        void exceptionIsRuntimeException() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getByUserId(1L));

            Assertions.assertInstanceOf(RuntimeException.class, ex);
        }

        @Test
        @DisplayName("Находит правильного пользователя среди нескольких")
        void findsCorrectUserAmongMany() {
            userRepository.save(new User(1L, "Tony"));
            userRepository.save(new User(2L, "Steve"));
            userRepository.save(new User(3L, "Natasha"));

            Assertions.assertEquals("Steve", userService.getByUserId(2L).displayName());
        }
    }

    @Nested
    @DisplayName("updateDisplayName")
    class UpdateDisplayNameTest {

        @Test
        @DisplayName("Обновляет имя существующего пользователя")
        void updatesExistingUser() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals("Iron Man",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, "Iron Man"));
        }

        @Test
        @DisplayName("Исключение содержит правильный идентификатор")
        void exceptionCarriesUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateDisplayName(999L, "Iron Man"));

            Assertions.assertEquals(999L, ex.userId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateDisplayName(1L, "Iron Man"));

            Assertions.assertTrue(userRepository.findByUserId(1L).isEmpty());
        }

        @Test
        @DisplayName("Сохраняет bio при обновлении имени")
        void keepsBioUnchanged() {
            User user = new User(1L, "Tony", "Genius", 0, 0, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals("Genius",
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }

        @Test
        @DisplayName("Сохраняет balance при обновлении имени")
        void keepsBalanceUnchanged() {
            User user = new User(1L, "Tony", "", 500, 0, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals(500,
                    userRepository.findByUserId(1L).orElseThrow().balance());
        }

        @Test
        @DisplayName("Сохраняет cardCount при обновлении имени")
        void keepsCardCountUnchanged() {
            User user = new User(1L, "Tony", "", 0, 10, null, null);
            userRepository.save(user);

            userService.updateDisplayName(1L, "Iron Man");

            Assertions.assertEquals(10,
                    userRepository.findByUserId(1L).orElseThrow().cardCount());
        }

        @Test
        @DisplayName("Обновляет имя на кириллицу — без валидации на уровне сервиса")
        void updatesToCyrillicName() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateDisplayName(1L, "Нарек");

            Assertions.assertEquals("Нарек",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
        }
    }

    @Nested
    @DisplayName("updateBio")
    class UpdateBioTest {

        @Test
        @DisplayName("Обновляет bio существующего пользователя")
        void updatesExistingUserBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Genius, billionaire, playboy, philanthropist");

            Assertions.assertEquals("Genius, billionaire, playboy, philanthropist",
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void throwsUserNotFoundExceptionWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(999L, "New bio"));
        }

        @Test
        @DisplayName("Исключение содержит правильный идентификатор")
        void exceptionCarriesUserId() {
            UserNotFoundException ex = Assertions.assertThrows(
                    UserNotFoundException.class,
                    () -> userService.updateBio(777L, "New bio"));

            Assertions.assertEquals(777L, ex.userId());
        }

        @Test
        @DisplayName("Не создаёт нового пользователя при обновлении")
        void doesNotCreateNewUser() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> userService.updateBio(1L, "New bio"));

            Assertions.assertTrue(userRepository.findByUserId(1L).isEmpty());
        }

        @Test
        @DisplayName("Сохраняет имя при обновлении bio")
        void keepsDisplayNameUnchanged() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "New bio");

            Assertions.assertEquals("Tony",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
        }

        @Test
        @DisplayName("Сохраняет balance при обновлении bio")
        void keepsBalanceUnchanged() {
            User user = new User(1L, "Tony", "", 500, 0, null, null);
            userRepository.save(user);

            userService.updateBio(1L, "New bio");

            Assertions.assertEquals(500,
                    userRepository.findByUserId(1L).orElseThrow().balance());
        }

        @Test
        @DisplayName("Сохраняет кириллицу и эмодзи")
        void savesCyrillicAndEmoji() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Привет! 🦸‍♂️");

            Assertions.assertEquals("Привет! 🦸‍♂️",
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }

        @Test
        @DisplayName("Пустое bio сохраняется как пустая строка")
        void savesEmptyBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "");

            Assertions.assertEquals("",
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }

        @Test
        @DisplayName("Многострочное bio сохраняется корректно")
        void savesMultilineBio() {
            userRepository.save(new User(1L, "Tony"));

            userService.updateBio(1L, "Line one\nLine two\nLine three");

            Assertions.assertEquals("Line one\nLine two\nLine three",
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }

        @Test
        @DisplayName("Длинное bio (256 символов) сохраняется")
        void savesMaxLengthBio() {
            userRepository.save(new User(1L, "Tony"));
            String longBio = "b".repeat(256);

            userService.updateBio(1L, longBio);

            Assertions.assertEquals(longBio,
                    userRepository.findByUserId(1L).orElseThrow().bio());
        }
    }

    /**
     * Фейковый репозиторий пользователей в памяти.
     */
    private class FakeUserRepository implements UserRepository {

        private final Map<Long, User> storage = new HashMap<>();

        @Override
        public Optional<User> findByUserId(long userId) {
            return Optional.ofNullable(storage.get(userId));
        }

        @Override
        public void save(User user) {
            storage.put(user.userId(), user);
        }

        @Override
        public void update(User user) {
            storage.put(user.userId(), user);
        }

        int size() {
            return storage.size();
        }
    }
}