package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.Command;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.repository.impl.UserRepositoryImpl;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class AvengersBot implements LongPollingSingleThreadUpdateConsumer {

    private final Logger log = LoggerFactory.getLogger(AvengersBot.class);
    private final TelegramClient telegramClient;
    private final CommandRegistry commandRegistry;

    public AvengersBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);

        DatabaseConfig dbConfig = new DatabaseConfig();
        UserRepository userRepository = new UserRepositoryImpl(dbConfig);
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();

        this.commandRegistry = new CommandRegistry(userService, validationService);
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();

        Command command = commandRegistry.resolve(text);
        try {
            command.execute(telegramClient, chatId, userId, userName,text);
        } catch (TelegramApiException e) {
            log.error("Error while executing the command", e);
        }
    }
}