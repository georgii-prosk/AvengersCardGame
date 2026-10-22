package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.MissingEnvironmentVariableException;

import java.util.Map;

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
    private final Map<String, String> envSource;

    /**
     * Создаёт reader, читающий из {@code System.getenv()}.
     */
    public EnvironmentConfigReader() {
        this(System.getenv());
    }

    /**
     * Создаёт reader, читающий значения из переданного отображения.
     * Отображение копируется, поэтому его последующие изменения
     * на reader не влияют.
     *
     * @param envSource отображение «имя переменной → значение»
     */
    public EnvironmentConfigReader(Map<String, String> envSource) {
        this.envSource = Map.copyOf(envSource);
    }

    @Override
    public AppConfig read() throws MissingEnvironmentVariableException {
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
     * @throws MissingEnvironmentVariableException если переменная не задана или пуста
     */
    private String require(String name) throws MissingEnvironmentVariableException {
        String value = envSource.get(name);
        if (value == null || value.isBlank()) {
            throw new MissingEnvironmentVariableException(name);
        }
        return value;
    }
}