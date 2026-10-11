package com.avengers.cardgame.config;

/**
 * Конфигурация приложения, считанная из переменных окружения.
 *
 * @param botToken токен Telegram-бота
 * @param database параметры подключения к базе данных
 */
public record EnvironmentConfig(String botToken, DatabaseConfig database) {

    /**
     * Имя переменной окружения с токеном бота.
     */
    private static final String BOT_TOKEN = "BOT_TOKEN";

    /**
     * Имя переменной окружения с JDBC URL.
     */
    private static final String DB_URL = "DB_URL";

    /**
     * Имя переменной окружения с именем пользователя БД.
     */
    private static final String DB_USER = "DB_USER";

    /**
     * Имя переменной окружения с паролем БД.
     */
    private static final String DB_PASSWORD = "DB_PASSWORD";

    /**
     * Читает конфигурацию из переменных окружения и проверяет, что все
     * обязательные переменные заданы.
     *
     * @return конфигурация приложения
     * @throws IllegalStateException если хотя бы одна переменная не задана
     */
    public static EnvironmentConfig fromEnvironment() {
        DatabaseConfig database = new DatabaseConfig(
                require(DB_URL),
                require(DB_USER),
                require(DB_PASSWORD)
        );
        return new EnvironmentConfig(require(BOT_TOKEN), database);
    }

    /**
     * Возвращает значение переменной окружения или падает, если её нет.
     *
     * @param name имя переменной
     * @return значение переменной
     * @throws IllegalStateException если переменная не задана или пуста
     */
    private static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set");
        }
        return value;
    }
}