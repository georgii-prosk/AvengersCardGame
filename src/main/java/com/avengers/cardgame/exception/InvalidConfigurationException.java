package com.avengers.cardgame.exception;

/**
 * Доменная ошибка конфигурации: некорректное значение параметра.
 */
public class InvalidConfigurationException extends AppException {

    /** Имя некорректного параметра. */
    private final String parameterName;

    /**
     * @param parameterName имя параметра
     * @param message       описание ошибки
     */
    public InvalidConfigurationException(String parameterName, String message) {
        super(message);
        this.parameterName = parameterName;
    }

    /**
     * @param parameterName имя параметра
     * @param message       описание ошибки
     * @param cause         причина
     */
    public InvalidConfigurationException(String parameterName, String message, Throwable cause) {
        super(message, cause);
        this.parameterName = parameterName;
    }

    /**
     * @return имя некорректного параметра
     */
    public String parameterName() {
        return parameterName;
    }
}