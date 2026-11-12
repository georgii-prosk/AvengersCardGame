package com.avengers.cardgame.service.command;

import java.util.List;
import java.util.Optional;

/**
 * Разбирает аргументы команд.
 */
public final class CommandArguments {

    /** Разделитель частей аргумента команды. */
    private static final String PART_SEPARATOR = "\\s+";

    /** Ожидаемое количество частей составного аргумента. */
    private static final int EXPECTED_PART_COUNT = 2;

    private CommandArguments() {
    }

    /**
     * Разбивает аргумент команды на части.
     *
     * @param argument аргумент команды
     * @return список частей аргумента
     */
    public static List<String> splitParts(String argument) {
        return List.of(argument.split(PART_SEPARATOR));
    }

    /**
     * Разбирает идентификатор (лота, карточки, героя).
     *
     * @param token часть аргумента команды
     * @return идентификатор, если разбор удался
     */
    public static Optional<Long> parseId(String token) {
        try {
            return Optional.of(Long.parseLong(token));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    /**
     * Разбирает число (количество, номер страницы).
     *
     * @param token часть аргумента команды
     * @return число, если разбор удался
     */
    public static Optional<Integer> parseNumber(String token) {
        try {
            return Optional.of(Integer.parseInt(token));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    /**
     * Разбирает составной аргумент «идентификатор + число».
     *
     * @param argument аргумент команды
     * @return пара идентификатор и число, если разбор удался
     */
    public static Optional<IdAndNumber> parseIdAndNumber(String argument) {
        List<String> parts = splitParts(argument);
        if (parts.size() != EXPECTED_PART_COUNT) {
            return Optional.empty();
        }
        Optional<Long> id = parseId(parts.getFirst());
        Optional<Integer> number = parseNumber(parts.getLast());
        if (id.isEmpty() || number.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new IdAndNumber(id.get(), number.get()));
    }

    /**
     * Пара «идентификатор + число».
     *
     * @param id     идентификатор
     * @param number число
     */
    public record IdAndNumber(long id, int number) {
    }
}
