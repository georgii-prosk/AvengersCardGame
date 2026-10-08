package com.avengers.cardgame.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Конфигурация и запуск миграций Flyway.
 * <p>
 * Отвечает за применение SQL-миграций из {@code src/main/resources/db/migration}
 * к базе данных при старте приложения. Параметры подключения берутся из
 * переменных окружения:
 * <ul>
 *     <li>{@code DB_URL} — JDBC-URL базы данных;</li>
 *     <li>{@code DB_USER} — имя пользователя;</li>
 *     <li>{@code DB_PASSWORD} — пароль.</li>
 * </ul>
 */
public class FlywayConfig {

    private final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    private final String url;
    private final String user;
    private final String password;

    /**
     * Рабочий конструктор. Читает параметры подключения из переменных окружения.
     */
    public FlywayConfig() {
        this(System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD"));
    }

    /**
     * Конструктор для тестов. Позволяет передать параметры напрямую
     * (например, из Testcontainers).
     *
     * @param url      JDBC URL базы данных
     * @param user     имя пользователя
     * @param password пароль
     */
    public FlywayConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Применяет все неприменённые миграции к базе данных.
     * <p>
     * Если миграции уже применены — ничего не делает (Flyway ведёт свою
     * историю в таблице {@code flyway_schema_history}).
     */
    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .load();
        flyway.migrate();
        log.info("Database migrations applied");
    }
}