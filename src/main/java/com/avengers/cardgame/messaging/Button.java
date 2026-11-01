package com.avengers.cardgame.messaging;

import org.jetbrains.annotations.NotNull;

/**
 * Кнопка под сообщением.
 *
 * @param text         надпись на кнопке
 * @param callbackData данные, которые вернутся боту при нажатии
 */
public record Button(@NotNull String text, String callbackData) {
}
