package com.avengers.cardgame.command;

/**
 * Обработчик команды бота.
 * <p>
 * Контракт без checked-исключений: реализация либо отправляет ответ
 * через {@code context.bot()}, либо отправляет сообщение об ошибке —
 * это уже забота {@link SafeCommandHandler}.
 */
public interface CommandHandler {

    /**
     * Обрабатывает команду, отправляя ответы через {@code context.bot()}.
     *
     * @param context контекст выполнения команды
     */
    void handle(CommandContext context);
}