package com.avengers.cardgame.config;

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
    private final Map<String, String> envSource;

    public EnvironmentConfigReader() { this(System.getenv()); }
    public EnvironmentConfigReader(Map<String, String> envSource) { this.envSource = Map.copyOf(envSource); }

    @Override
    public AppConfig read() throws MissingEnvironmentVariableException {
        DatabaseConfig database = new DatabaseConfig(require(DB_URL), require(DB_USER), require(DB_PASSWORD));
        GameConfig game = new GameConfig(
                Duration.ofMinutes(readPositiveLong(DAILY_COOLDOWN_MINUTES, 1440)),
                Duration.ofMinutes(readPositiveLong(NOTIFICATION_INTERVAL_MINUTES, 30)));
        return new AppConfig(require(BOT_TOKEN), database, game);
    }

    private String require(String name) throws MissingEnvironmentVariableException {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) throw new MissingEnvironmentVariableException(name);
        return value;
    }

    private long readPositiveLong(String name, long defaultValue) {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) return defaultValue;
        try {
            long parsed = Long.parseLong(value);
            if (parsed <= 0) throw new IllegalArgumentException(name + " must be positive");
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric configuration: " + name, e);
        }
    }
}
