package com.avengers.cardgame.command;

/**
 * Найденная команда вместе с аргументом запроса.
 *
 * @param command  найденная команда
 * @param argument текст после имени команды
 */
public record ResolvedCommand(Command command, String argument) {
}