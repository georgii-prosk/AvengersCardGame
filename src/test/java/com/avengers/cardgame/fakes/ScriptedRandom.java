package com.avengers.cardgame.fakes;

import java.util.Random;

/**
 * Генератор для тестов: {@link #nextInt(int)} возвращает заранее заданные
 * значения по очереди и проверяет, что они укладываются в запрошенную границу.
 */
public class ScriptedRandom extends Random {

    /** Значения, которые нужно вернуть. */
    private final int[] values;

    /** Индекс следующего значения. */
    private int next;

    /**
     * @param values значения для последовательных вызовов {@code nextInt(bound)}
     */
    public ScriptedRandom(int... values) {
        this.values = values;
    }

    @Override
    public int nextInt(int bound) {
        if (next >= values.length) {
            throw new IllegalStateException("ScriptedRandom has no more values");
        }
        int value = values[next++];
        if (value < 0 || value >= bound) {
            throw new IllegalStateException("Scripted value " + value + " is out of bound " + bound);
        }
        return value;
    }
}
