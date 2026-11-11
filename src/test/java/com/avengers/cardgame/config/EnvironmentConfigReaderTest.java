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
     * @param environment отображение «имя → значение»
     * @return reader
     */
    private EnvironmentConfigReader readerWith(Map<String, String> environment) {
        return new EnvironmentConfigReader(environment);
    }

    /**
     * Возвращает отображение со всеми обязательными переменными.
     *
     * @return отображение
     */
    private Map<String, String> validEnvironment() {
        Map<String, String> environment = new HashMap<>();
        environment.put("BOT_TOKEN", "test-token");
        environment.put("DB_URL", "jdbc:postgresql://localhost:5432/test");
        environment.put("DB_USER", "user");
        environment.put("DB_PASSWORD", "pass");
        return environment;
    }

    @Test
    @DisplayName("Читает валидную конфигурацию")
    void readsValidConfig() throws ConfigurationException {
        AppConfig configuration = readerWith(validEnvironment()).read();

        Assertions.assertEquals("test-token", configuration.botToken());
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/test",
                configuration.database().url());
        Assertions.assertEquals("user", configuration.database().user());
        Assertions.assertEquals("pass", configuration.database().password());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если BOT_TOKEN не задан")
    void throwsWhenBotTokenMissing() {
        Map<String, String> environment = validEnvironment();
        environment.remove("BOT_TOKEN");

        MissingEnvironmentVariableException exception = Assertions.assertThrows(
                MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());

        Assertions.assertEquals("BOT_TOKEN", exception.variableName());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_URL не задан")
    void throwsWhenDbUrlMissing() {
        Map<String, String> environment = validEnvironment();
        environment.remove("DB_URL");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_USER не задан")
    void throwsWhenDbUserMissing() {
        Map<String, String> environment = validEnvironment();
        environment.remove("DB_USER");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException, если DB_PASSWORD не задан")
    void throwsWhenDbPasswordMissing() {
        Map<String, String> environment = validEnvironment();
        environment.remove("DB_PASSWORD");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());
    }

    @Test
    @DisplayName("Бросает MissingEnvironmentVariableException при пустом значении")
    void throwsWhenValueBlank() {
        Map<String, String> environment = validEnvironment();
        environment.put("BOT_TOKEN", "   ");

        Assertions.assertThrows(MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());
    }

    @Test
    @DisplayName("Сообщение исключения содержит имя переменной")
    void exceptionMessageContainsVariableName() {
        Map<String, String> environment = validEnvironment();
        environment.remove("DB_URL");

        MissingEnvironmentVariableException exception = Assertions.assertThrows(
                MissingEnvironmentVariableException.class,
                () -> readerWith(environment).read());

        Assertions.assertTrue(exception.getMessage().contains("DB_URL"));
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для нечислового кулдауна")
    void throwsWhenCooldownNotNumeric() {
        Map<String, String> environment = validEnvironment();
        environment.put("DAILY_COOLDOWN_MINUTES", "abc");

        InvalidConfigurationException exception = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(environment).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", exception.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для нулевого кулдауна")
    void throwsWhenCooldownZero() {
        Map<String, String> environment = validEnvironment();
        environment.put("DAILY_COOLDOWN_MINUTES", "0");

        InvalidConfigurationException exception = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(environment).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", exception.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для отрицательного кулдауна")
    void throwsWhenCooldownNegative() {
        Map<String, String> environment = validEnvironment();
        environment.put("DAILY_COOLDOWN_MINUTES", "-5");

        InvalidConfigurationException exception = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(environment).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", exception.parameterName());
    }

    @Test
    @DisplayName("Бросает InvalidConfigurationException для слишком большого кулдауна")
    void throwsWhenCooldownTooLarge() {
        Map<String, String> environment = validEnvironment();
        environment.put("DAILY_COOLDOWN_MINUTES", "999999999999999999999");

        InvalidConfigurationException exception = Assertions.assertThrows(InvalidConfigurationException.class,
                () -> readerWith(environment).read());
        Assertions.assertEquals("DAILY_COOLDOWN_MINUTES", exception.parameterName());
    }

    @Test
    @DisplayName("Использует дефолтный кулдаун, если переменная не задана")
    void usesDefaultCooldownWhenNotSet() throws ConfigurationException {
        AppConfig configuration = readerWith(validEnvironment()).read();

        Assertions.assertEquals(Duration.ofMinutes(1440), configuration.game().dailyCooldown());
    }

    @Test
    @DisplayName("Использует дефолтные название и эмодзи валюты")
    void usesDefaultCurrencyWhenNotSet() throws ConfigurationException {
        AppConfig configuration = readerWith(validEnvironment()).read();

        Assertions.assertEquals("Кристаллы", configuration.game().currencyName());
        Assertions.assertEquals("💎", configuration.game().currencyEmoji());
    }

    @Test
    @DisplayName("Читает кастомные название и эмодзи валюты")
    void readsCustomCurrency() throws ConfigurationException {
        Map<String, String> environment = validEnvironment();
        environment.put("CURRENCY_NAME", "Щиты");
        environment.put("CURRENCY_EMOJI", "🛡️");

        AppConfig configuration = readerWith(environment).read();

        Assertions.assertEquals("Щиты", configuration.game().currencyName());
        Assertions.assertEquals("🛡️", configuration.game().currencyEmoji());
    }
}