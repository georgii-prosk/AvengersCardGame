package com.avengers.cardgame.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Применяет миграции Flyway к базе данных.
 */
public class DatabaseMigrator {

    private final Logger log = LoggerFactory.getLogger(DatabaseMigrator.class);

    private final DatabaseConfig config;

    public DatabaseMigrator(DatabaseConfig config) {
        this.config = config;
    }

    /**
     * Применяет все неприменённые миграции из classpath.
     */
    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(config.url(), config.user(), config.password())
                .load();
        flyway.migrate();
        log.info("Database migrations applied");
    }
}