package com.avengers.cardgame.bot;


import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Тесты для {@link TelegramBot}.
 */
class TelegramBotTest {

    private List<String> sentTexts;
    private TelegramClient client;
    private TelegramBot bot;

    @BeforeEach
    void setUp() {
        sentTexts = new ArrayList<>();
        client = createFakeTelegramClient(sentTexts);

        UserRepository userRepository = new FakeUserRepository();
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();

        CommandRegistry registry =
                new CommandRegistryFactory(userService, validationService).create();

        bot = new TelegramBot(client, registry);
    }

    @Test
    @DisplayName("Обрабатывает /start и отправляет приветствие")
    void consume_startCommand_sendsGreeting() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/start"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("AvengersCardGame"));
    }

    @Test
    @DisplayName("Обрабатывает /help и отправляет список команд")
    void consume_helpCommand_sendsHelp() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/help"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("/start"));
        Assertions.assertTrue(sentTexts.getLast().contains("/help"));
    }

    @Test
    @DisplayName("Отправляет сообщение о неизвестной команде")
    void consume_unknownCommand_sendsUnknownMessage() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/unknowncmd"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Неизвестная команда"));
        Assertions.assertTrue(sentTexts.getLast().contains("/help"));
    }

    @Test
    @DisplayName("Игнорирует Update без сообщения")
    void consume_updateWithoutMessage_doesNothing() {
        Update update = new Update();
        bot.consume(update);
        Assertions.assertTrue(sentTexts.isEmpty());
    }

    @Test
    @DisplayName("Игнорирует сообщение без текста")
    void consume_messageWithoutText_doesNothing() {
        Chat chat = Chat.builder().id(10L).type("private").build();
        Message message = Message.builder().chat(chat).build();

        Update update = new Update();
        update.setMessage(message);

        bot.consume(update);

        Assertions.assertTrue(sentTexts.isEmpty());
    }

    @Test
    @DisplayName("Обрабатывает /setname с валидным именем")
    void consume_setNameCommand_updatesName() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/setname Narek"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Narek"));
    }

    @Test
    @DisplayName("Обрабатывает /setname с невалидным именем")
    void consume_setNameInvalid_sendsError() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/setname Нарек"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("некорректное имя"));
    }

    @Test
    @DisplayName("Обрабатывает /setbio с валидным bio")
    void consume_setBioCommand_updatesBio() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/setbio Привет 🦸"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Привет"));
    }

    @Test
    @DisplayName("Обрабатывает /profile и отправляет данные")
    void consume_profileCommand_sendsProfile() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/profile"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Tony"));
    }

    @Test
    @DisplayName("Использует Avenger при отсутствии username")
    void consume_profile_withoutUsername_usesDefaultName() {
        bot.consume(createUpdate(1L, 10L, null, "/profile"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Avenger"));
    }

    @Test
    @DisplayName("/setnameX foo не считается командой /setname")
    void consume_setNameWithoutSpace_isUnknown() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/setnameX foo"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Неизвестная команда"));
    }

    private Update createUpdate(long userId, long chatId, String userName, String text) {
        String safeFirstName = (userName == null || userName.isBlank()) ? "Unknown" : userName;

        org.telegram.telegrambots.meta.api.objects.User.UserBuilder userBuilder =
                org.telegram.telegrambots.meta.api.objects.User.builder()
                        .id(userId)
                        .firstName(safeFirstName)
                        .isBot(false);

        if (userName != null && !userName.isBlank()) {
            userBuilder.userName(userName);
        }

        org.telegram.telegrambots.meta.api.objects.User from = userBuilder.build();

        Chat chat = Chat.builder()
                .id(chatId)
                .type("private")
                .build();

        Message message = Message.builder()
                .from(from)
                .chat(chat)
                .text(text)
                .build();

        Update update = new Update();
        update.setMessage(message);
        return update;
    }

    private static TelegramClient createFakeTelegramClient(List<String> sentTexts) {
        return (TelegramClient) Proxy.newProxyInstance(
                TelegramClient.class.getClassLoader(),
                new Class<?>[]{TelegramClient.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if (("execute".equals(name) || "executeAsync".equals(name))
                            && args != null && args.length > 0
                            && args[0] instanceof SendMessage sendMessage) {
                        sentTexts.add(sendMessage.getText());
                    }
                    if (CompletableFuture.class.isAssignableFrom(method.getReturnType())) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return null;
                }
        );
    }

    private class FakeUserRepository implements UserRepository {

        private final Map<Long, User> storage = new HashMap<>();

        @Override
        public Optional<User> findByUserId(long userId) {
            return Optional.ofNullable(storage.get(userId));
        }

        @Override
        public void save(User user) {
            storage.put(user.userId(), user);
        }

        @Override
        public void update(User user) {
            storage.put(user.userId(), user);
        }
    }
}