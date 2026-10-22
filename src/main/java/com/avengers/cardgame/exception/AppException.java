package com.avengers.cardgame.exception;

/**
 * Базовое доменное исключение приложения.
 * <p>
 * Все доменные ошибки наследуются от этого класса. Позволяет ловить
 * одним {@code catch (AppException e)} любую ошибку бизнес-логики,
 * не перечисляя конкретные типы.
 * <p>
 * Класс абстрактный: бросать именно {@code AppException} нельзя —
 * только конкретных наследников.
 */
public abstract class AppException extends Exception {

    /**
     * Создаёт исключение с описанием.
     *
     * @param message описание ошибки
     */
    protected AppException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с описанием и причиной.
     *
     * @param message описание ошибки
     * @param cause   причина
     */
    protected AppException(String message, Throwable cause) {
        super(message, cause);
    }
}