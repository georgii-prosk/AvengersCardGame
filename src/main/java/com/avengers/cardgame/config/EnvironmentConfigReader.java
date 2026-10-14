package com.avengers.cardgame.config;

import java.util.function.Function;

/**
 * Читает {@link AppConfig} из переменных окружения.
 */
public class EnvironmentConfigReader implements ConfigProvider {

    /** Имя переменной окружения с токеном бота. */
    private static final String BOT_TOKEN = "BOT_TOKEN";

    /** Имя переменной окружения с JDBC URL. */
    private static final String DB_URL = "DB_URL";

    /** Имя переменной окружения с именем пользователя БД. */
    private static final String DB_USER = "DB_USER";

    /** Имя переменной окружения с паролем БД. */
    private static final String DB_PASSWORD = "DB_PASSWORD";

    /** Источник значений переменных. */
    private final Function<String, String> envSource;

    /**
     * Создаёт reader, читающий из {@code System.getenv()}.
     */
    public EnvironmentConfigReader() {
        this(System::getenv);
    }

    /**
     * Создаёт reader с указанным источником значений.
     * Используется в тестах.
     *
     * @param envSource функция, возвращающая значение по имени переменной
     */
    public EnvironmentConfigReader(Function<String, String> envSource) {
        this.envSource = envSource;
    }

    /**
     * Читает конфигурацию приложения из источника переменных окружения
     * и проверяет, что все обязательные переменные заданы.
     *
     * @return конфигурация приложения
     * @throws IllegalStateException если хотя бы одна переменная не задана
     *                               или пуста
     */
    @Override
    public AppConfig read() {
        DatabaseConfig database = new DatabaseConfig(
                require(DB_URL),
                require(DB_USER),
                require(DB_PASSWORD)
        );
        return new AppConfig(require(BOT_TOKEN), database);
    }

    /**
     * Возвращает значение переменной или падает, если её нет.
     *
     * @param name имя переменной
     * @return значение переменной
     * @throws IllegalStateException если переменная не задана или пуста
     */
    private String require(String name) {
        String value = envSource.apply(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set");
        }
        return value;
    }
}