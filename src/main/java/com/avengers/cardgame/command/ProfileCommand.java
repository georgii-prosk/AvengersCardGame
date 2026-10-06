package com.avengers.cardgame.command;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class ProfileCommand implements Command {

    private final UserService userService;

    public ProfileCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String name() {
        return "/profile";
    }

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