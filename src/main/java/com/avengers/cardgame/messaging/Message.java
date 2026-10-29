package com.avengers.cardgame.messaging;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Сообщение для отправки пользователю.
 *
 * @param text    текст сообщения
 * @param buttons inline-кнопки под сообщением, сгруппированные по рядам;
 *                пустой список означает отсутствие клавиатуры
 */
public record Message(@NotNull String text, List<List<Button>> buttons) {

    /**
     * Создаёт сообщение с защитной копией кнопок.
     *
     * @param text    текст сообщения
     * @param buttons ряды кнопок, {@code null} воспринимается как «без кнопок»
     */
    public Message {
        buttons = buttons == null
                ? List.of()
                : buttons.stream().map(List::copyOf).toList();
    }

    /**
     * Создаёт сообщение без кнопок.
     *
     * @param text текст сообщения
     */
    public Message(String text) {
        this(text, List.of());
    }
}
