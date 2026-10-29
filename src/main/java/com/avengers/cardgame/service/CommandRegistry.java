package com.avengers.cardgame.service;

import java.util.Optional;

/** Реестр команд бота. */
public interface CommandRegistry {
    void register(String name, CommandHandler command);
    Optional<ResolvedCommand> resolve(String text);
}