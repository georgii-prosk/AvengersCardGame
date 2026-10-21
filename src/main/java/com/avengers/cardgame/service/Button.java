package com.avengers.cardgame.service;

/**
 * Inline-кнопка под сообщением.
 *
 * @param text         надпись на кнопке
 * @param callbackData данные, которые вернутся боту при нажатии
 */
public record Button(String text, String callbackData) {
}
