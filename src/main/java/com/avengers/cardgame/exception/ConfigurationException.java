package com.avengers.cardgame.exception;

/**
 * Базовое исключение для ошибок конфигурации приложения.
 */
public abstract class ConfigurationException extends AppException {

    /**
     * @param message описание ошибки
     */
    protected ConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message описание ошибки
     * @param cause   причина
     */
    protected ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}