package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр команд Telegram-бота.
 * <p>
 * Хранит команды в виде отображения «имя → команда», регистрирует их в конструкторе
 * и разрешает входящий текст в соответствующую реализацию {@link Command}.
 */
public class CommandRegistry {

    /** Карта зарегистрированных команд: имя команды → реализация. */
    private final Map<String, Command> commands = new HashMap<>();

    /**
     * Создаёт реестр и регистрирует все доступные команды.
     *
     * @param userService       сервис для работы с пользователями
     * @param validationService сервис валидации входных данных
     */
    public CommandRegistry(UserService userService, ValidationService validationService) {
        register(new StartCommand(userService));
        register(new HelpCommand());
        register(new ProfileCommand(userService));
        register(new SetNameCommand(userService, validationService));
        register(new SetBioCommand(userService, validationService));
    }

    /**
     * Регистрирует команду в реестре.
     *
     * @param command команда для регистрации
     */
    private void register(Command command) {
        commands.put(command.name(), command);
    }

    /**
     * Определяет команду по тексту сообщения.
     * <p>
     * Из текста берётся первое слово, приводится к нижнему регистру и ищется
     * в реестре. Если текст пустой или команда не найдена — возвращается
     * {@link UnknownCommand}.
     *
     * @param text текст входящего сообщения
     * @return найденная команда или {@link UnknownCommand}
     */
    public Command resolve(String text) {
        if (text == null || text.isBlank()) {
            return new UnknownCommand();
        }
        String commandName = text.trim().split("\\s+")[0].toLowerCase();
        return commands.getOrDefault(commandName, new UnknownCommand());
    }
}