package com.avengers.cardgame.service;

import java.util.regex.Pattern;

/**
 * Сервис валидации пользовательских данных.
 * <p>
 * Проверяет корректность отображаемого имени и описания профиля
 * с учётом ограничений по длине и допустимым символам.
 */
public class ValidationService {

    /** Максимальная длина отображаемого имени. */
    private static final int NAME_MAX_LENGTH = 128;

    /** Максимальная длина описания профиля. */
    private static final int BIO_MAX_LENGTH = 256;

    /** Шаблон допустимого имени: только латинские буквы. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    /**
     * Шаблон допустимого описания профиля: латиница, кириллица, цифры,
     * пробелы, эмодзи, знаки препинания и ряд спецсимволов.
     */
    private static final Pattern BIO_PATTERN = Pattern.compile(
            "^[a-zA-Zа-яА-ЯёЁ0-9\\s" +
                    "\\p{So}\\p{Sk}\\p{Sc}" +                       // эмодзи, модификаторы, валютные символы
                    "/|\\\\<>@#$%^&*(){}`~;:'\"\\-_=+\\[\\].,!?]+$"
    );

    /**
     * Проверяет корректность отображаемого имени.
     * <p>
     * Имя должно быть непустым, длиной не более {@value #NAME_MAX_LENGTH}
     * символов и содержать только латинские буквы.
     *
     * @param name проверяемое имя
     * @return {@code true}, если имя корректно, иначе {@code false}
     */
    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (name.length() > NAME_MAX_LENGTH) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Проверяет корректность описания профиля.
     * <p>
     * Описание должно быть непустым, длиной не более {@value #BIO_MAX_LENGTH}
     * символов и содержать только допустимые символы (латиница, кириллица,
     * цифры, пробелы, эмодзи, знаки препинания и разрешённые спецсимволы).
     *
     * @param bio проверяемое описание
     * @return {@code true}, если описание корректно, иначе {@code false}
     */
    public boolean isValidBio(String bio) {
        if (bio == null || bio.isBlank()) {
            return false;
        }
        if (bio.length() > BIO_MAX_LENGTH) {
            return false;
        }
        return BIO_PATTERN.matcher(bio).matches();
    }
}