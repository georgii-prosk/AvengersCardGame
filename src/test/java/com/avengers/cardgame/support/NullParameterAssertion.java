package com.avengers.cardgame.support;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

/**
 * Проверки отклонения {@code null}-значений параметров, помеченных {@code @NotNull}.
 * <p>
 * При обычном запуске через Maven проверки {@code Objects.requireNonNull} выбрасывают
 * {@link NullPointerException}. IntelliJ IDEA включает runtime-инструментацию JetBrains annotations
 * и выбрасывает {@link IllegalArgumentException} ещё до входа в тело метода, поэтому тесты
 * должны принимать оба варианта.
 */
public final class NullParameterAssertion {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private NullParameterAssertion() {
    }

    /**
     * Проверяет, что действие отклоняет переданный {@code null}.
     *
     * @param executable действие, которое должно завершиться ошибкой
     */
    public static void assertRejected(Executable executable) {
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, executable);
        boolean rejected = exception instanceof NullPointerException
                || exception instanceof IllegalArgumentException;
        Assertions.assertTrue(rejected,
                "Ожидалось NullPointerException или IllegalArgumentException, но было: "
                        + exception.getClass().getName());
    }
}
