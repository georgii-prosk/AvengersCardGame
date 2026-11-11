package com.avengers.cardgame.service;

import java.util.Optional;

/**
 * Реестр команд бота. Позволяет регистрировать обработчики и находить их по тексту сообщения.
 */
public interface CommandRegistry {

    /**
     * Регистрирует обработчик для указанной команды.
     *
     * @param name    имя команды (например, "/start")
     * @param command обработчик команды
     */
    void register(String name, CommandHandler command);

    /**
     * Находит команду и её аргумент по тексту сообщения.
     *
     * @param text полный текст сообщения (например, "/setname Tony")
     * @return объект с найденной командой и аргументом, либо пустой Optional, если команда не найдена
     */
    Optional<ResolvedCommand> resolve(String text);
}