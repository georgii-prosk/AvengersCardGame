package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidBioException;

import java.util.regex.Pattern;

/**
 * Описание профиля пользователя.
 *
 * @param value значение описания
 */
public record Bio(String value) {

    /** Максимальная длина описания. */
    private static final int MAX_LENGTH = 256;

    /** Шаблон допустимых символов. */
    private static final Pattern PATTERN = Pattern.compile(
            "^[a-zA-Zа-яА-ЯёЁ0-9\\s"
                    + "\\p{So}\\p{Sk}\\p{Sc}"
                    + "/|\\\\<>@#$%^&*(){}`~;:'\"\\-_=+\\[\\].,!?]+$"
    );

    /**
     * Создаёт описание с проверкой инвариантов.
     * <p>
     * Предпочтительнее использовать {@link #of(String)}.
     *
     * @param value значение описания
     * @throws IllegalArgumentException если описание пустое, длиннее 256 символов
     *                                  или содержит недопустимые символы
     */
    public Bio {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Bio must not be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Bio must be at most " + MAX_LENGTH + " characters");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "Bio contains invalid characters");
        }
    }

    /**
     * Создаёт описание с проверкой инвариантов.
     *
     * @param value значение описания
     * @return валидный объект описания
     * @throws InvalidBioException если описание пустое, длиннее 256 символов
     *                             или содержит недопустимые символы
     */
    public static Bio of(String value) throws InvalidBioException {
        try {
            return new Bio(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidBioException(exception.getMessage());
        }
    }
}