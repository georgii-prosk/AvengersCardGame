package com.avengers.cardgame.config;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

/**
 * Читает {@link GameConfig} из файла {@code game-config.properties}.
 */
public class GameConfigReader {

    /** Имя файла в classpath. */
    private static final String FILE_NAME = "game-config.properties";

    /**
     * Читает конфигурацию.
     *
     * @return игровая конфигурация
     * @throws IllegalStateException если файл не найден
     * @throws RuntimeException      при ошибке чтения или парсинга
     */
    public GameConfig read() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(FILE_NAME)) {
            if (in == null) {
                throw new IllegalStateException("File " + FILE_NAME + " not found in classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + FILE_NAME, e);
        }

        long cooldownMinutes = Long.parseLong(props.getProperty("daily.cooldown.minutes", "1440"));
        long notifyIntervalMinutes = Long.parseLong(props.getProperty("daily.notification.interval.minutes", "30"));

        return new GameConfig(
                Duration.ofMinutes(cooldownMinutes),
                Duration.ofMinutes(notifyIntervalMinutes)
        );
    }
}