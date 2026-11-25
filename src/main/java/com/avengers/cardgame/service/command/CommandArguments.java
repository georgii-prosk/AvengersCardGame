package com.avengers.cardgame.service.command;

import java.util.List;
import java.util.Optional;

/**
 * Разбирает аргументы команд.
 */
public final class CommandArguments {

    /** Разделитель частей аргумента команды. */
    private static final String PART_SEPARATOR = "\\s+";

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
}
