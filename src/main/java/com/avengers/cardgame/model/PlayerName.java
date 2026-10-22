package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidPlayerNameException;

import java.util.regex.Pattern;

/**
 * Отображаемое имя пользователя.
 *
 * @param value значение имени
 */
public record PlayerName(String value) {

    /** Максимальная длина имени. */
    private static final int MAX_LENGTH = 128;

    /** Шаблон допустимого имени: только латинские буквы. */
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z]+$");

    /**
     * Создаёт имя с проверкой инвариантов.
     * <p>
     * Предпочтительнее использовать {@link #of(String)}.
     *
     * @param value значение имени
     * @throws IllegalArgumentException если имя невалидно
     */
    public PlayerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Display name must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Display name must be at most " + MAX_LENGTH + " characters");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Display name must contain only Latin letters");
        }
    }

    /**
     * Создаёт имя с проверкой инвариантов.
     *
     * @param value значение имени
     * @return валидный объект имени
     * @throws InvalidPlayerNameException если имя невалидно
     */
    public static PlayerName of(String value) throws InvalidPlayerNameException {
        try {
            return new PlayerName(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidPlayerNameException(e.getMessage());
        }
    }
}