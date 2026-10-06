package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда {@code /setname <name>} — меняет отображаемое имя пользователя.
 * <p>
 * Имя проходит валидацию через {@link ValidationService#isValidName(String)}.
 * При некорректном вводе отправляется сообщение с правилами.
 */
public class SetNameCommand implements Command {

    /** Сервис для работы с пользователями. */
    private final UserService userService;

    /** Сервис валидации входных данных. */
    private final ValidationService validationService;

    /**
     * Создаёт команду смены отображаемого имени.
     *
     * @param userService       сервис пользователей
     * @param validationService сервис валидации
     */
    public SetNameCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public String name() {
        return "/setname";
    }

    /**
     * Проверяет аргумент, обновляет имя и уведомляет пользователя.
     *
     * @param client   клиент Telegram
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param text     текст сообщения с аргументом
     * @throws TelegramApiException при ошибке отправки сообщения
     */
    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        // Отрезаем "/setname" и берём всё, что после первого пробела
        String newName = extractArgument(text, "/setname");

        if (newName.isEmpty() || !validationService.isValidName(newName)) {
            send(client, chatId,
                    "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.");
            return;
        }

        userService.registerIfAbsent(userId, "Avenger");
        userService.updateDisplayName(userId, newName);

        send(client, chatId, "Вы сменили отображаемое имя на " + newName);
    }

    /**
     * Извлекает аргумент команды из текста сообщения.
     *
     * @param text    полный текст сообщения
     * @param command имя команды
     * @return аргумент после команды или пустая строка
     */
    private String extractArgument(String text, String command) {
        String trimmed = text.trim();
        if (trimmed.length() <= command.length()) {
            return "";
        }
        return trimmed.substring(command.length()).trim();
    }

    /**
     * Отправляет текстовое сообщение в чат.
     *
     * @param client  клиент Telegram
     * @param chatId  идентификатор чата
     * @param message текст сообщения
     * @throws TelegramApiException при ошибке отправки
     */
    private void send(TelegramClient client, long chatId, String message)
            throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        client.execute(sendMessage);
    }
}