package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда {@code /start} — запускает бота и приветствует пользователя.
 * <p>
 * Регистрирует пользователя в системе (если он ещё не зарегистрирован).
 * Если у пользователя нет username в Telegram, используется имя по умолчанию
 * {@code "Avenger"}.
 */
public class StartCommand implements Command {

    /** Сервис для работы с пользователями. */
    private final UserService userService;

    /**
     * Создаёт команду запуска бота.
     *
     * @param userService сервис пользователей
     */
    public StartCommand(UserService userService) {
        this.userService = userService;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code "/start"}
     */
    @Override
    public String name() {
        return "/start";
    }

    /**
     * Регистрирует пользователя и отправляет приветственное сообщение.
     *
     * @param client   клиент Telegram
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя в Telegram (может быть {@code null})
     * @param text     текст сообщения
     * @throws TelegramApiException при ошибке отправки сообщения
     */
    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        String defaultName = (userName == null || userName.isBlank()) ? "Avenger" : userName;
        userService.registerIfAbsent(userId, defaultName);

        String message = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        client.execute(sendMessage);
    }
}