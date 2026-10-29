package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InvalidConfigurationException;
import com.avengers.cardgame.exception.MissingEnvironmentVariableException;

import java.time.Duration;
import java.util.Map;

public class EnvironmentConfigReader implements ConfigProvider {
    private static final String BOT_TOKEN = "BOT_TOKEN";
    private static final String DB_URL = "DB_URL";
    private static final String DB_USER = "DB_USER";
    private static final String DB_PASSWORD = "DB_PASSWORD";
    private static final String DAILY_COOLDOWN_MINUTES = "DAILY_COOLDOWN_MINUTES";
    private static final String NOTIFICATION_INTERVAL_MINUTES = "DAILY_NOTIFICATION_INTERVAL_MINUTES";

    private static final long DEFAULT_DAILY_COOLDOWN_MINUTES = 1440;
    private static final long DEFAULT_NOTIFICATION_INTERVAL_MINUTES = 30;
    private static final long MAX_DURATION_MINUTES = Long.MAX_VALUE / 60_000_000L;

    private final Map<String, String> envSource;

    public EnvironmentConfigReader() { this(System.getenv()); }
    public EnvironmentConfigReader(Map<String, String> envSource) { this.envSource = Map.copyOf(envSource); }

    @Override
    public AppConfig read() throws AppException {
        DatabaseConfig database = new DatabaseConfig(
                require(DB_URL), require(DB_USER), require(DB_PASSWORD));
        GameConfig game = new GameConfig(
                Duration.ofMinutes(readPositiveLong(DAILY_COOLDOWN_MINUTES, DEFAULT_DAILY_COOLDOWN_MINUTES)),
                Duration.ofMinutes(readPositiveLong(NOTIFICATION_INTERVAL_MINUTES, DEFAULT_NOTIFICATION_INTERVAL_MINUTES)));
        return new AppConfig(require(BOT_TOKEN), database, game);
    }

    private String require(String name) throws MissingEnvironmentVariableException {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) throw new MissingEnvironmentVariableException(name);
        return value;
    }

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
}