package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
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
 * Тесты для {@link AvengersBot}.
 * <p>
 * Проверяют маршрутизацию апдейтов и обработку некорректных сообщений.
 * Без БД, без сети — только фейковый {@link TelegramClient}.
 */
class AvengersBotTest {

    private List<String> sentTexts;
    private TelegramClient client;
    private AvengersBot bot;

    @BeforeEach
    void setUp() {
        sentTexts = new ArrayList<>();
        client = createFakeTelegramClient(sentTexts);

        UserRepository userRepository = new FakeUserRepository();
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();
        CommandRegistry registry = new CommandRegistry(userService, validationService);

        bot = new AvengersBot(client, registry);
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
    @DisplayName("Обрабатывает неизвестную команду")
    void consume_unknownCommand_sendsUnknownMessage() {
        bot.consume(createUpdate(1L, 10L, "Tony", "/unknowncmd"));

        Assertions.assertFalse(sentTexts.isEmpty());
        Assertions.assertTrue(sentTexts.getLast().contains("Неизвестная команда"));
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
        Chat chat = Chat.builder()
                .id(10L)
                .type("private")
                .build();
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
        Assertions.assertTrue(sentTexts.getLast().contains("Avenger"));
    }

    // ===== Хелперы =====

    /**
     * Создаёт Update с текстовым сообщением от указанного пользователя.
     * <p>
     * В TelegramBots 7.10.0 у {@code User} обязательны поля {@code id}, {@code firstName}
     * и {@code isBot}, у {@link Chat} — {@code id} и {@code type}.
     * Без них builder падает с NullPointerException.
     *
     * @param userId   Telegram ID пользователя
     * @param chatId   ID чата
     * @param userName username и firstName пользователя
     * @param text     текст сообщения
     * @return готовый Update
     */
    private Update createUpdate(long userId, long chatId, String userName, String text) {
        org.telegram.telegrambots.meta.api.objects.User from =
                org.telegram.telegrambots.meta.api.objects.User.builder()
                        .id(userId)
                        .firstName(userName)
                        .userName(userName)
                        .isBot(false)
                        .build();

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

    /**
     * Создаёт фейковый клиент Telegram через динамический прокси.
     * <p>
     * Перехватывает вызовы {@code execute} и {@code executeAsync},
     * сохраняет текст {@link SendMessage} в переданный список
     * и возвращает заглушку.
     *
     * @param sentTexts список, куда складывать отправленные тексты
     * @return прокси-объект, реализующий {@link TelegramClient}
     */
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

    /**
     * Фейковый репозиторий пользователей в памяти.
     * <p>
     * Полноценная реализация {@link UserRepository} без обращения к БД.
     */
    private class FakeUserRepository implements UserRepository {

        /** Хранилище пользователей по Telegram ID. */
        private final Map<Long, User> storage = new HashMap<>();

        @Override
        public Optional<User> findByTelegramId(long telegramId) {
            return Optional.ofNullable(storage.get(telegramId));
        }

        @Override
        public void save(User user) {
            storage.put(user.getTelegramId(), user);
        }

        @Override
        public void update(User user) {
            storage.put(user.getTelegramId(), user);
        }
    }
}