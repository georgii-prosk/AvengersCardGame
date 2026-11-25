package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.ConfigurationException;
import com.avengers.cardgame.exception.InvalidConfigurationException;
import com.avengers.cardgame.exception.MissingEnvironmentVariableException;
import com.avengers.cardgame.model.HeroRarity;

import java.time.Duration;
import java.util.Map;

/**
 * Реализация {@link ConfigProvider}, читающая конфигурацию из переменных окружения.
 */
public class EnvironmentConfigReader implements ConfigProvider {
    /** Имя переменной окружения с токеном Telegram-бота. */
    private static final String BOT_TOKEN = "BOT_TOKEN";

    /** Имя переменной окружения с JDBC URL базы данных. */
    private static final String DB_URL = "DB_URL";

    /** Имя переменной окружения с именем пользователя БД. */
    private static final String DB_USER = "DB_USER";

    /** Имя переменной окружения с паролем БД. */
    private static final String DB_PASSWORD = "DB_PASSWORD";

    /** Имя переменной окружения с временем кулдауна ежедневной награды (в минутах). */
    private static final String DAILY_COOLDOWN_MINUTES = "DAILY_COOLDOWN_MINUTES";

    /** Имя переменной окружения с интервалом проверки уведомлений (в минутах). */
    private static final String NOTIFICATION_INTERVAL_MINUTES = "DAILY_NOTIFICATION_INTERVAL_MINUTES";

    /** Имя переменной окружения с названием игровой валюты. */
    private static final String CURRENCY_NAME = "CURRENCY_NAME";

    /** Имя переменной окружения с эмодзи игровой валюты. */
    private static final String CURRENCY_EMOJI = "CURRENCY_EMOJI";

    /** Имя переменной окружения с эмодзи игровых кусочков. */
    private static final String SHARDS_EMOJI = "SHARDS_EMOJI";

    /** Имя переменной окружения с комиссией рынка (в процентах). */
    private static final String MARKET_COMMISSION_PERCENT = "MARKET_COMMISSION_PERCENT";

    /** Значение кулдауна по умолчанию (1440 минут = 24 часа). */
    private static final long DEFAULT_DAILY_COOLDOWN_MINUTES = 1440;

    /** Значение интервала уведомлений по умолчанию (30 минут). */
    private static final long DEFAULT_NOTIFICATION_INTERVAL_MINUTES = 30;

    /** Название валюты по умолчанию. */
    private static final String DEFAULT_CURRENCY_NAME = "Кристаллы";

    /** Эмодзи валюты по умолчанию. */
    private static final String DEFAULT_CURRENCY_EMOJI = "💎";

    /** Эмодзи кусочков по умолчанию. */
    private static final String DEFAULT_SHARDS_EMOJI = "🧩";

    /** Комиссия рынка по умолчанию (в процентах). */
    private static final int DEFAULT_MARKET_COMMISSION_PERCENT = 5;

    /** Максимально допустимое количество минут, чтобы избежать переполнения при создании Duration. */
    private static final long MAX_DURATION_MINUTES = Long.MAX_VALUE / 60_000_000L;

    /** Источник значений переменных окружения. */
    private final Map<String, String> envSource;

    /**
     * Создаёт reader, читающий переменные из {@link System#getenv()}.
     */
    public EnvironmentConfigReader() { this(System.getenv()); }

    /**
     * Создаёт reader с заданным источником переменных.
     *
     * @param envSource карта с переменными окружения
     */
    public EnvironmentConfigReader(Map<String, String> envSource) { this.envSource = Map.copyOf(envSource); }

    @Override
    public AppConfig read() throws ConfigurationException {
        DatabaseConfig database = new DatabaseConfig(
                require(DB_URL), require(DB_USER), require(DB_PASSWORD));
        Map<HeroRarity, GameConfig.RarityConfig> rarityConfigs = Map.of(
                HeroRarity.COMMON, new GameConfig.RarityConfig(1, 5, 10, 200),
                HeroRarity.RARE, new GameConfig.RarityConfig(3, 15, 50, 1000),
                HeroRarity.EPIC, new GameConfig.RarityConfig(8, 40, 200, 5000),
                HeroRarity.LEGENDARY, new GameConfig.RarityConfig(20, 100, 1000, 25000)
        );
        GameConfig game;
        try {
            game = new GameConfig(
                    Duration.ofMinutes(readPositiveLong(DAILY_COOLDOWN_MINUTES, DEFAULT_DAILY_COOLDOWN_MINUTES)),
                    Duration.ofMinutes(readPositiveLong(NOTIFICATION_INTERVAL_MINUTES,
                            DEFAULT_NOTIFICATION_INTERVAL_MINUTES)),
                    readNonBlank(CURRENCY_NAME, DEFAULT_CURRENCY_NAME),
                    readNonBlank(CURRENCY_EMOJI, DEFAULT_CURRENCY_EMOJI),
                    readNonBlank(SHARDS_EMOJI, DEFAULT_SHARDS_EMOJI),
                    rarityConfigs,
                    readMarketCommissionPercent());
        } catch (IllegalArgumentException exception) {
            throw new InvalidConfigurationException(MARKET_COMMISSION_PERCENT, exception.getMessage(), exception);
        }
        return new AppConfig(require(BOT_TOKEN), database, game);
    }

    /**
     * Возвращает значение обязательной переменной окружения.
     *
     * @param name имя переменной
     * @return непустое значение переменной
     * @throws MissingEnvironmentVariableException если переменная отсутствует или содержит только пробелы
     */
    private String require(String name) throws MissingEnvironmentVariableException {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) throw new MissingEnvironmentVariableException(name);
        return value;
    }

    /**
     * Читает непустое строковое значение из переменной окружения.
     * <p>
     * Если переменная не задана или содержит только пробелы, возвращает значение по умолчанию.
     *
     * @param name         имя переменной
     * @param defaultValue значение по умолчанию
     * @return непустое значение переменной или значение по умолчанию
     */
    private String readNonBlank(String name, String defaultValue) {
        String value = envSource.get(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * Читает числовое значение из переменной окружения, проверяя его на положительность
     * и максимально допустимый предел.
     * <p>
     * Если переменная не задана или пуста, возвращает переданное значение по умолчанию.
     *
     * @param name         имя переменной
     * @param defaultValue значение по умолчанию (используется, если переменная не задана)
     * @return валидное положительное число
     * @throws InvalidConfigurationException если значение не является числом,
     *                                       меньше или равно нулю, либо слишком велико
     */
    private long readPositiveLong(String name, long defaultValue) throws InvalidConfigurationException {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new InvalidConfigurationException(name,
                        "Configuration parameter " + name + " must be positive, got: " + parsed);
            }
            if (parsed > MAX_DURATION_MINUTES) {
                throw new InvalidConfigurationException(name,
                        "Configuration parameter " + name + " is too large: " + parsed);
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new InvalidConfigurationException(name,
                    "Invalid numeric configuration: " + name, exception);
        }
    }

    /**
     * Читает комиссию рынка из переменной окружения.
     * <p>
     * Если переменная не задана или пуста, возвращает значение по умолчанию.
     * Диапазон допустимых значений проверяет конструктор {@link GameConfig}.
     *
     * @return комиссия рынка в процентах
     * @throws InvalidConfigurationException если значение не является числом
     */
    private int readMarketCommissionPercent() throws InvalidConfigurationException {
        String value = envSource.get(MARKET_COMMISSION_PERCENT);
        if (value == null || value.isBlank()) {
            return DEFAULT_MARKET_COMMISSION_PERCENT;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidConfigurationException(MARKET_COMMISSION_PERCENT,
                    "Invalid numeric configuration: " + MARKET_COMMISSION_PERCENT, exception);
        }
    }
}
