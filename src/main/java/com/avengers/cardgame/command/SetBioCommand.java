package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда {@code /setbio <bio>} — обновляет описание профиля пользователя.
 * <p>
 * Описание проходит валидацию через {@link ValidationService#isValidBio(String)}.
 * При некорректном вводе отправляется сообщение с правилами.
 */
public class SetBioCommand implements Command {

    /** Сервис для работы с пользователями. */
    private final UserService userService;

    /** Сервис валидации входных данных. */
    private final ValidationService validationService;

    /**
     * Создаёт команду изменения описания профиля.
     *
     * @param userService       сервис пользователей
     * @param validationService сервис валидации
     */
    public SetBioCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public String name() {
        return "/setbio";
    }

    /**
     * Проверяет аргумент, обновляет описание профиля и уведомляет пользователя.
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

        String newBio = extractArgument(text, "/setbio");

        if (newBio.isEmpty() || !validationService.isValidBio(newBio)) {
            send(client, chatId,
                    "Вы ввели некорректное описание профиля. Допустимо использовать до 256 символов " +
                            "латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, " +
                            "спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).");
            return;
        }

        userService.registerIfAbsent(userId, "Avenger");
        userService.updateBio(userId, newBio);

        send(client, chatId, "Вы обновили описание своего профиля на " + newBio);
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