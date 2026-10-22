package com.avengers.cardgame.exception;

/**
 * Ошибка конфигурации: обязательная переменная окружения не задана или пуста.
 */
public class MissingEnvironmentVariableException extends AppException {

    /** Имя отсутствующей переменной. */
    private final String variableName;

    /**
     * @param variableName имя отсутствующей переменной
     */
    public MissingEnvironmentVariableException(String variableName) {
        super("Environment variable " + variableName + " is not set");
        this.variableName = variableName;
    }

    /**
     * @return имя отсутствующей переменной
     */
    public String variableName() {
        return variableName;
    }
}