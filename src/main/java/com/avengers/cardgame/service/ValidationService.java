package com.avengers.cardgame.service;

import java.util.regex.Pattern;

public class ValidationService {

    private static final int NAME_MAX_LENGTH = 128;
    private static final int BIO_MAX_LENGTH = 256;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    private static final Pattern BIO_PATTERN = Pattern.compile(
            "^[a-zA-Zа-яА-ЯёЁ0-9\\s" +
                    "\\p{So}\\p{Sk}\\p{Sc}" +                       // эмодзи, модификаторы, валютные символы
                    "/|\\\\<>@#$%^&*(){}`~;:'\"\\-_=+\\[\\].,!?]+$"
    );

    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (name.length() > NAME_MAX_LENGTH) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

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