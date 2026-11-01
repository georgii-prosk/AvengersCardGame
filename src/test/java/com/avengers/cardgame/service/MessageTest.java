package com.avengers.cardgame.service;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link Message} и {@link Button}.
 */
class MessageTest {

    @Test
    @DisplayName("Конструктор только с текстом создаёт сообщение без кнопок")
    void textOnlyConstructorHasNoButtons() {
        Message message = new Message("Привет");

        Assertions.assertEquals("Привет", message.text());
        Assertions.assertTrue(message.buttons().isEmpty());
    }

    @Test
    @DisplayName("null вместо кнопок превращается в пустой список")
    void nullButtonsBecomeEmpty() {
        Message message = new Message("Привет", null);

        Assertions.assertTrue(message.buttons().isEmpty());
    }

    @Test
    @DisplayName("Хранит защитную копию кнопок")
    void keepsDefensiveCopy() {
        List<Button> row = new ArrayList<>(List.of(new Button("A", "a:1")));
        List<List<Button>> rows = new ArrayList<>(List.of(row));

        Message message = new Message("Текст", rows);
        row.add(new Button("B", "b:2"));
        rows.clear();

        Assertions.assertEquals(1, message.buttons().size());
        Assertions.assertEquals(1, message.buttons().getFirst().size());
    }
}
