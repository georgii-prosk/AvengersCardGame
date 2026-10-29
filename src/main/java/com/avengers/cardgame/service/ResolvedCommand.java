package com.avengers.cardgame.service;

/**
 * Найденная команда вместе с аргументом запроса.
 *
 * @param command  найденный обработчик
 * @param argument текст после имени команды
 */
public record ResolvedCommand(CommandHandler command, String argument) {
}