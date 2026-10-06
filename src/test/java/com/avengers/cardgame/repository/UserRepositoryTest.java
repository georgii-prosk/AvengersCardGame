package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.model.User;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Интеграционные тесты для {@link UserRepositoryImpl}.
 * <p>
 * Используется PostgreSQL-контейнер Testcontainers. Миграции применяются
 * через Flyway — те же, что и в проде.
 */
@Testcontainers
class UserRepositoryImplTest {

    /** Общий контейнер PostgreSQL для всех тестов. */
    private static PostgreSQLContainer<?> postgres;

    /** Конфигурация БД, указывающая на тестовый контейнер. */
    private DatabaseConfig dbConfig;

    /** Тестируемый репозиторий. */
    private UserRepositoryImpl userRepository;

    /**
     * Запускает контейнер и применяет миграции Flyway один раз для всех тестов.
     */
    @BeforeAll
    static void startContainer() {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();

        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load()
                .migrate();
    }

    /**
     * Останавливает контейнер после всех тестов.
     */
    @AfterAll
    static void stopContainer() {
        if (postgres != null) {
            postgres.stop();
        }
    }

    /**
     * Создаёт конфигурацию и репозиторий перед каждым тестом,
     * а также очищает таблицу {@code users}.
     *
     * @throws SQLException при ошибке очистки
     */
    @BeforeEach
    void setUp() throws SQLException {
        dbConfig = new TestDatabaseConfig(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword()
        );
        userRepository = new UserRepositoryImpl(dbConfig);
        try (Connection conn = dbConfig.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        }
    }

    /**
     * Тесты для {@link UserRepositoryImpl#findByTelegramId(long)}.
     */
    @Nested
    @DisplayName("findByTelegramId")
    class FindByTelegramId {

        /**
         * Проверяет возврат существующего пользователя.
         */
        @Test
        @DisplayName("Возвращает пользователя, если он найден")
        void shouldReturnUserWhenFound() {
            userRepository.save(new User(1L, "Avenger"));
            userRepository.update(withBio(1L, "bio"));

            Optional<User> found = userRepository.findByTelegramId(1L);

            Assertions.assertTrue(found.isPresent());
            Assertions.assertEquals(1L, found.get().getTelegramId());
            Assertions.assertEquals("Avenger", found.get().getDisplayName());
            Assertions.assertEquals("bio", found.get().getBio());
        }

        /**
         * Проверяет пустой результат для отсутствующего пользователя.
         */
        @Test
        @DisplayName("Возвращает пустой Optional, если пользователь не найден")
        void shouldReturnEmptyWhenNotFound() {
            Assertions.assertTrue(userRepository.findByTelegramId(999L).isEmpty());
        }
    }

    /**
     * Тесты для {@link UserRepositoryImpl#save(User)}.
     */
    @Nested
    @DisplayName("save")
    class Save {

        /**
         * Проверяет сохранение нового пользователя.
         */
        @Test
        @DisplayName("Сохраняет нового пользователя")
        void shouldSaveNewUser() {
            userRepository.save(new User(42L, "IronMan"));

            Optional<User> found = userRepository.findByTelegramId(42L);
            Assertions.assertTrue(found.isPresent());
            Assertions.assertEquals("IronMan", found.get().getDisplayName());
        }
    }

    /**
     * Тесты для {@link UserRepositoryImpl#update(User)}.
     */
    @Nested
    @DisplayName("update")
    class Update {

        /**
         * Проверяет обновление display_name и bio.
         */
        @Test
        @DisplayName("Обновляет display_name и bio пользователя")
        void shouldUpdateUser() {
            User user = new User(7L, "OldName");
            userRepository.save(user);

            user.setDisplayName("NewName");
            user.setBio("new bio");
            userRepository.update(user);

            Optional<User> found = userRepository.findByTelegramId(7L);
            Assertions.assertTrue(found.isPresent());
            Assertions.assertEquals("NewName", found.get().getDisplayName());
            Assertions.assertEquals("new bio", found.get().getBio());
        }
    }

    /**
     * Вспомогательный метод — возвращает пользователя с заданным bio.
     *
     * @param id  Telegram ID
     * @param bio описание
     * @return пользователь
     */
    private User withBio(long id, String bio) {
        User user = new User(id, "Avenger");
        user.setBio(bio);
        return user;
    }

    /**
     * Реализация {@link DatabaseConfig} для тестов: параметры подключения
     * задаются напрямую, минуя переменные окружения.
     */
    private static class TestDatabaseConfig extends DatabaseConfig {

        /** JDBC URL тестовой БД. */
        private final String url;

        /** Имя пользователя тестовой БД. */
        private final String user;

        /** Пароль тестовой БД. */
        private final String password;

        /**
         * Создаёт тестовую конфигурацию.
         *
         * @param url      JDBC URL
         * @param user     имя пользователя
         * @param password пароль
         */
        TestDatabaseConfig(String url, String user, String password) {
            this.url = url;
            this.user = user;
            this.password = password;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Connection getConnection() throws SQLException {
            return DriverManager.getConnection(url, user, password);
        }
    }
}