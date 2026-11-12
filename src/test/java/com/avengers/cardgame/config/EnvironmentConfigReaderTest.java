package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.ConfigurationException;
import com.avengers.cardgame.exception.MissingEnvironmentVariableException;
import com.avengers.cardgame.exception.InvalidConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.time.Duration;

/**
 * Тесты для {@link EnvironmentConfigReader}.
 */
class EnvironmentConfigReaderTest {

    /**
     * Создаёт reader с заданными переменными окружения.
     *
     * @param env отображение «имя → значение»
     * @return reader
     */
    private EnvironmentConfigReader readerWith(Map<String, String> env) {
        return new EnvironmentConfigReader(env);
    }

    /**
     * Возвращает отображение со всеми обязательными переменными.
     *
     * @return отображение
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
    void readsValidConfig() throws ConfigurationException {
        AppConfig config = readerWith(validEnv()).read();

        Assertions.assertEquals("test-token", config.botToken());
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/test",
                config.database().url());
        Assertions.assertEquals("user", config.database().user());
        Assertions.assertEquals("pass", config.database().password());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если BOT_TOKEN не задан")
    void throwsWhenBotTokenMissing() {
        Map<String, String> env = validEnv();
        env.remove("BOT_TOKEN");

        MissingEnvironmentVariableException ex = Assertions.assertThrows(
                MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());

        Assertions.assertEquals("BOT_TOKEN", ex.variableName());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_URL не задан")
    void throwsWhenDbUrlMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_URL");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_USER не задан")
    void throwsWhenDbUserMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_USER");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_PASSWORD не задан")
    void throwsWhenDbPasswordMissing() {
        Map<String, String> env = validEnv();
        env.remove("DB_PASSWORD");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException при пустом значении")
    void throwsWhenValueBlank() {
        Map<String, String> env = validEnv();
        env.put("BOT_TOKEN", "   ");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());
    }

    @Test
    @DisplayName("Сообщение исключения содержит имя переменной")
    void exceptionMessageContainsVariableName() {
        Map<String, String> env = validEnv();
        env.remove("DB_URL");

        MissingEnvironmentVariableException ex = Assertions.assertThrows(
                MissingEnvironmentVariableException.class,
                () -> readerWith(env).read());

        Assertions.assertTrue(ex.getMessage().contains("DB_URL"));
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для нечислового кулдауна")
    void throwsWhenCooldownNotNumeric() {
        Map<String, String> env = validEnv();
        env.put("DAILY_COOLDOWN_MINUTES", "abc");

        InvalidConfigurationException ex = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(env).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", ex.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для нулевого кулдауна")
    void throwsWhenCooldownZero() {
        Map<String, String> env = validEnv();
        env.put("DAILY_COOLDOWN_MINUTES", "0");

        InvalidConfigurationException ex = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(env).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", ex.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для отрицательного кулдауна")
    void throwsWhenCooldownNegative() {
        Map<String, String> env = validEnv();
        env.put("DAILY_COOLDOWN_MINUTES", "-5");

        InvalidConfigurationException ex = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(env).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", ex.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для слишком большого кулдауна")
    void throwsWhenCooldownTooLarge() {
        Map<String, String> env = validEnv();
        env.put("DAILY_COOLDOWN_MINUTES", "999999999999999999999");

        InvalidConfigurationException ex = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(env).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", ex.parameterName());
    }

    @Test
    @DisplayName("Использует дефолтный кулдаун, если переменная не задана")
    void usesDefaultCooldownWhenNotSet() throws ConfigurationException {
        AppConfig config = readerWith(validEnv()).read();

        Assertions.assertEquals(Duration.ofMinutes(1440), config.game().dailyCooldown());
    }

    @Test
    @DisplayName("Использует валюту по умолчанию, если переменные не заданы")
    void usesDefaultCurrencyWhenNotSet() throws ConfigurationException {
        AppConfig config = readerWith(validEnv()).read();

        Assertions.assertEquals(GameConfig.DEFAULT_CURRENCY_NAME, config.game().currencyName());
        Assertions.assertEquals(GameConfig.DEFAULT_CURRENCY_EMOJI, config.game().currencyEmoji());
    }

    @Test
    @DisplayName("Читает настраиваемое название и эмодзи валюты")
    void readsCustomCurrency() throws ConfigurationException {
        Map<String, String> env = validEnv();
        env.put("CURRENCY_NAME", "Гири");
        env.put("CURRENCY_EMOJI", "🪙");

        AppConfig config = readerWith(env).read();

        Assertions.assertEquals("Гири", config.game().currencyName());
        Assertions.assertEquals("🪙", config.game().currencyEmoji());
    }

    @Test
    @DisplayName("Использует эмодзи кусочков по умолчанию, если переменная не задана")
    void usesDefaultShardsEmojiWhenNotSet() throws ConfigurationException {
        AppConfig config = readerWith(validEnv()).read();

        Assertions.assertEquals(GameConfig.DEFAULT_SHARDS_EMOJI, config.game().shardsEmoji());
    }

    @Test
    @DisplayName("Читает настраиваемый эмодзи кусочков")
    void readsCustomShardsEmoji() throws ConfigurationException {
        Map<String, String> env = validEnv();
        env.put("SHARDS_EMOJI", "⭐");

        AppConfig config = readerWith(env).read();

        Assertions.assertEquals("⭐", config.game().shardsEmoji());
    }
}