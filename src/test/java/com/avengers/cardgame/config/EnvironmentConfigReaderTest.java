package com.avengers.cardgame.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Тесты для {@link EnvironmentConfigReader}.
 */
class EnvironmentConfigReaderTest {

    /**
     * Создаёт reader с заданными переменными окружения.
     *
     * @param env карта «имя → значение»
     * @return reader
     */
    private EnvironmentConfigReader readerWith(Map<String, String> env) {
        return new EnvironmentConfigReader(env::get);
    }

    /**
     * Возвращает карту со всеми обязательными переменными.
     *
     * @return карта
     */
    private Map<String, String> validEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("BOT_TOKEN", "test-token");
        env.put("DB_URL", "jdbc:postgresql://localhost:5432/test");
        env.put("DB_USER", "user");
        env.put("DB_PASSWORD", "pass");
        return env;
    }

    @Test
    @DisplayName("Читает валидную конфигурацию")
    void readsValidConfig() {
        AppConfig config = readerWith(validEnv()).read();

        Assertions.assertEquals("test-token", config.botToken());
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/test",
                config.database().url());
        Assertions.assertEquals("user", config.database().user());
        Assertions.assertEquals("pass", config.database().password());
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если BOT_TOKEN не задан")
    void throwsWhenBotTokenMissing() {
        Map<String, String> env = validEnv();
        env.remove("BOT_TOKEN");

        Assertions.assertThrows(IllegalStateException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если DB_URL не задан")
    void throwsWhenDbUrlMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_URL");

        Assertions.assertThrows(IllegalStateException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если DB_USER не задан")
    void throwsWhenDbUserMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_USER");

        Assertions.assertThrows(IllegalStateException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если DB_PASSWORD не задан")
    void throwsWhenDbPasswordMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_PASSWORD");

        Assertions.assertThrows(IllegalStateException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает IllegalStateException при пустом значении")
    void throwsWhenValueBlank() {
        Map<String, String> env = validEnv();
        env.put("BOT_TOKEN", "   ");

        Assertions.assertThrows(IllegalStateException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Сообщение исключения содержит имя переменной")
    void exceptionMessageContainsVariableName() {
        Map<String, String> env = validEnv();
        env.remove("DB_URL");

        IllegalStateException ex = Assertions.assertThrows(
                IllegalStateException.class,
                () -> readerWith(env).read());

        Assertions.assertTrue(ex.getMessage().contains("DB_URL"));
    }
}