package com.avengers.cardgame.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Применяет миграции Flyway к базе данных.
 */
public class DatabaseMigrator {

    /** Логгер. */
    private final Logger logger = LoggerFactory.getLogger(DatabaseMigrator.class);

    /** Параметры подключения к базе данных. */
    private final DatabaseConfig databaseConfiguration;

    /**
     * @param databaseConfiguration параметры подключения к базе данных
     */
    public DatabaseMigrator(DatabaseConfig databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    /**
     * Применяет все неприменённые миграции из classpath.
     */
    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(databaseConfiguration.url(), databaseConfiguration.user(),
                        databaseConfiguration.password())
                .load();
        flyway.migrate();
        logger.info("Database migrations applied");
    }
}