package com.avengers.cardgame.command;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда {@code /profile} — показывает профиль пользователя: имя, описание,
 * баланс щитов и количество карточек в коллекции.
 * <p>
 * Если пользователь ещё не зарегистрирован, он создаётся автоматически
 * с именем по умолчанию {@code "Avenger"}.
 */
public class ProfileCommand implements Command {

    /** Сервис для работы с пользователями. */
    private final UserService userService;

    /**
     * Создаёт команду профиля.
     *
     * @param userService сервис пользователей
     */
    public ProfileCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String name() {
        return "/profile";
    }

    /**
     * Регистрирует пользователя (при необходимости) и отправляет его профиль в чат.
     *
     * @param client   клиент Telegram
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param text     текст сообщения
     * @throws TelegramApiException при ошибке отправки сообщения
     */
    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        User user = userService.registerIfAbsent(userId, "Avenger");

        String displayName = user.getDisplayName() == null || user.getDisplayName().isBlank()
                ? "не задано"
                : user.getDisplayName();

        String bio = user.getBio() == null || user.getBio().isBlank()
                ? "не задано"
                : user.getBio();

        String message = """
                Имя: *%s*
                Описание: *%s*
                Твой баланс: *%d* щитов
                В твоей коллекции: *%d* карточек
                """.formatted(displayName, bio, user.getBalance(), user.getCardCount());

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .parseMode("Markdown")
                .build();

        client.execute(sendMessage);
    }
}