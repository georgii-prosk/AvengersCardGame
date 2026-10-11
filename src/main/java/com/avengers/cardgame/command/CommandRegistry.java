package com.avengers.cardgame.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр команд бота.
 */
public class CommandRegistry {

    /** Карта зарегистрированных команд: имя → реализация. */
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Регистрирует команду под указанным именем.
     *
     * @param name    имя команды вида {@code "/start"}
     * @param command реализация команды
     */
    public void register(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Определяет команду по тексту сообщения.
     *
     * @param text полный текст сообщения
     * @return команда с аргументом, если найдена
     */
    public Optional<ResolvedCommand> resolve(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        int spaceIndex = trimmed.indexOf(' ');
        String name = (spaceIndex == -1 ? trimmed : trimmed.substring(0, spaceIndex)).toLowerCase();
        Command command = commands.get(name);
        if (command == null) {
            return Optional.empty();
        }
        String argument = (spaceIndex == -1) ? "" : trimmed.substring(spaceIndex + 1).trim();
        return Optional.of(new ResolvedCommand(command, argument));
    }
}