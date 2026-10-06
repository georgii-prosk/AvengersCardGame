package com.avengers.cardgame.command;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Тесты для команд Telegram-бота.
 * <p>
 * Используются ручные фейковые реализации {@link TelegramClient},
 * {@link UserRepository} и {@link ValidationService} — без Mockito.
 */
class CommandTest {

    /** Фейковый клиент Telegram (динамический прокси). */
    private TelegramClient client;

    /** Список текстов, отправленных через фейковый клиент. */
    private final List<String> sentTexts = new ArrayList<>();

    /** Фейковый репозиторий пользователей в памяти. */
    private FakeUserRepository userRepository;

    /** Сервис пользователей поверх фейкового репозитория. */
    private UserService userService;

    /** Фейковый сервис валидации с настраиваемыми результатами. */
    private FakeValidationService validationService;

    /**
     * Создаёт фейки и сервисы перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        sentTexts.clear();
        client = createFakeTelegramClient(sentTexts);
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
        validationService = new FakeValidationService();
    }

    /**
     * Возвращает текст последнего отправленного сообщения.
     *
     * @return текст последнего сообщения
     */
    private String lastText() {
        Assertions.assertFalse(sentTexts.isEmpty(), "Сообщение не было отправлено");
        return sentTexts.getLast();
    }

    /**
     * Создаёт фейковый клиент Telegram через динамический прокси.
     * <p>
     * Прокси перехватывает вызовы {@code execute} и {@code executeAsync},
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
     * Тесты для {@link StartCommand}.
     */
    @Nested
    @DisplayName("StartCommand")
    class StartCommandTest {

        @Test
        @DisplayName("Возвращает имя /start")
        void shouldReturnName() {
            Assertions.assertEquals("/start", new StartCommand(userService).name());
        }

        @Test
        @DisplayName("Регистрирует пользователя и отправляет приветствие")
        void shouldRegisterAndSendGreeting() throws TelegramApiException {
            new StartCommand(userService).execute(client, 10L, 1L, "Tony", "/start");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isPresent());
            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
            Assertions.assertTrue(lastText().contains("AvengersCardGame"));
        }

        @Test
        @DisplayName("Использует Avenger при отсутствии username")
        void shouldUseDefaultNameWhenUsernameMissing() throws TelegramApiException {
            new StartCommand(userService).execute(client, 10L, 1L, null, "/start");

            Assertions.assertEquals("Avenger",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }

        @Test
        @DisplayName("Не перезаписывает существующего пользователя")
        void shouldNotOverwriteExistingUser() throws TelegramApiException {
            userRepository.save(new User(1L, "Original"));

            new StartCommand(userService).execute(client, 10L, 1L, "Tony", "/start");

            Assertions.assertEquals("Original",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }
    }

    /**
     * Тесты для {@link HelpCommand}.
     */
    @Nested
    @DisplayName("HelpCommand")
    class HelpCommandTest {

        @Test
        @DisplayName("Возвращает имя /help")
        void shouldReturnName() {
            Assertions.assertEquals("/help", new HelpCommand().name());
        }

        @Test
        @DisplayName("Отправляет список команд")
        void shouldSendHelpMessage() throws TelegramApiException {
            new HelpCommand().execute(client, 10L, 1L, "Tony", "/help");

            String text = lastText();
            Assertions.assertTrue(text.contains("/start"));
            Assertions.assertTrue(text.contains("/help"));
            Assertions.assertTrue(text.contains("/profile"));
            Assertions.assertTrue(text.contains("/setname"));
            Assertions.assertTrue(text.contains("/setbio"));
        }
    }

    /**
     * Тесты для {@link ProfileCommand}.
     */
    @Nested
    @DisplayName("ProfileCommand")
    class ProfileCommandTest {

        @Test
        @DisplayName("Возвращает имя /profile")
        void shouldReturnName() {
            Assertions.assertEquals("/profile", new ProfileCommand(userService).name());
        }

        @Test
        @DisplayName("Отправляет данные профиля")
        void shouldSendProfileData() throws TelegramApiException {
            userRepository.save(new User(1L, "Tony", "Genius", 100, 5, null, null));

            new ProfileCommand(userService).execute(client, 10L, 1L, "Tony", "/profile");

            String text = lastText();
            Assertions.assertTrue(text.contains("Tony"));
            Assertions.assertTrue(text.contains("Genius"));
            Assertions.assertTrue(text.contains("100"));
            Assertions.assertTrue(text.contains("5"));
        }

        @Test
        @DisplayName("Отображает «не задано» для пустых полей")
        void shouldShowDefaultForEmptyFields() throws TelegramApiException {
            userRepository.save(new User(1L, "", "", 0, 0, null, null));

            new ProfileCommand(userService).execute(client, 10L, 1L, "Tony", "/profile");

            Assertions.assertTrue(lastText().contains("не задано"));
        }

        @Test
        @DisplayName("Регистрирует пользователя при отсутствии")
        void shouldRegisterIfAbsent() throws TelegramApiException {
            new ProfileCommand(userService).execute(client, 10L, 1L, "Tony", "/profile");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isPresent());
        }
    }

    /**
     * Тесты для {@link SetNameCommand}.
     */
    @Nested
    @DisplayName("SetNameCommand")
    class SetNameCommandTest {

        @Test
        @DisplayName("Возвращает имя /setname")
        void shouldReturnName() {
            Assertions.assertEquals("/setname",
                    new SetNameCommand(userService, validationService).name());
        }

        @Test
        @DisplayName("Обновляет имя при корректном вводе")
        void shouldUpdateNameWhenValid() throws TelegramApiException {
            validationService.validName = true;

            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname Tony");

            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
            Assertions.assertTrue(lastText().contains("Tony"));
        }

        @Test
        @DisplayName("Не обновляет имя при невалидном вводе")
        void shouldNotUpdateNameWhenInvalid() throws TelegramApiException {
            validationService.validName = false;

            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname bad name");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(lastText().contains("некорректное имя"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() throws TelegramApiException {
            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(lastText().contains("некорректное имя"));
        }
    }

    /**
     * Тесты для {@link SetBioCommand}.
     */
    @Nested
    @DisplayName("SetBioCommand")
    class SetBioCommandTest {

        @Test
        @DisplayName("Возвращает имя /setbio")
        void shouldReturnName() {
            Assertions.assertEquals("/setbio",
                    new SetBioCommand(userService, validationService).name());
        }

        @Test
        @DisplayName("Обновляет bio при корректном вводе")
        void shouldUpdateBioWhenValid() throws TelegramApiException {
            validationService.validBio = true;

            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio new bio");

            Assertions.assertEquals("new bio",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
            Assertions.assertTrue(lastText().contains("new bio"));
        }

        @Test
        @DisplayName("Не обновляет bio при невалидном вводе")
        void shouldNotUpdateBioWhenInvalid() throws TelegramApiException {
            validationService.validBio = false;

            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio bad bio");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(lastText().contains("некорректное описание"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() throws TelegramApiException {
            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(lastText().contains("некорректное описание"));
        }
    }

    /**
     * Тесты для {@link UnknownCommand}.
     */
    @Nested
    @DisplayName("UnknownCommand")
    class UnknownCommandTest {

        @Test
        @DisplayName("Возвращает имя /unknown")
        void shouldReturnName() {
            Assertions.assertEquals("/unknown", new UnknownCommand().name());
        }

        @Test
        @DisplayName("Отправляет сообщение о неизвестной команде")
        void shouldSendUnknownMessage() throws TelegramApiException {
            new UnknownCommand().execute(client, 10L, 1L, "Tony", "/foo");

            String text = lastText();
            Assertions.assertTrue(text.contains("Неизвестная команда"));
            Assertions.assertTrue(text.contains("/help"));
        }
    }

    /**
     * Тесты для {@link CommandRegistry}.
     */
    @Nested
    @DisplayName("CommandRegistry")
    class CommandRegistryTest {

        /** Тестируемый реестр команд. */
        private CommandRegistry registry;

        @BeforeEach
        void setUp() {
            registry = new CommandRegistry(userService, validationService);
        }

        @Test
        @DisplayName("Разрешает /start")
        void shouldResolveStart() {
            Assertions.assertInstanceOf(StartCommand.class, registry.resolve("/start"));
        }

        @Test
        @DisplayName("Разрешает /help")
        void shouldResolveHelp() {
            Assertions.assertInstanceOf(HelpCommand.class, registry.resolve("/help"));
        }

        @Test
        @DisplayName("Разрешает /profile")
        void shouldResolveProfile() {
            Assertions.assertInstanceOf(ProfileCommand.class, registry.resolve("/profile"));
        }

        @Test
        @DisplayName("Разрешает /setname")
        void shouldResolveSetName() {
            Assertions.assertInstanceOf(SetNameCommand.class, registry.resolve("/setname"));
        }

        @Test
        @DisplayName("Разрешает /setbio")
        void shouldResolveSetBio() {
            Assertions.assertInstanceOf(SetBioCommand.class, registry.resolve("/setbio"));
        }

        @Test
        @DisplayName("Разрешает команду в любом регистре")
        void shouldResolveCaseInsensitive() {
            Assertions.assertInstanceOf(StartCommand.class, registry.resolve("/START"));
        }

        @Test
        @DisplayName("Игнорирует аргументы команды")
        void shouldIgnoreArguments() {
            Assertions.assertInstanceOf(SetNameCommand.class,
                    registry.resolve("/setname Tony"));
        }

        @Test
        @DisplayName("Возвращает UnknownCommand для неизвестной команды")
        void shouldReturnUnknownForUnknownCommand() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve("/foo"));
        }

        @Test
        @DisplayName("Возвращает UnknownCommand для null")
        void shouldReturnUnknownForNull() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve(null));
        }

        @Test
        @DisplayName("Возвращает UnknownCommand для пустой строки")
        void shouldResolveBlankAsUnknown() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve("   "));
        }
    }

    /**
     * Фейковый репозиторий пользователей в памяти.
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

    /**
     * Фейковый сервис валидации с настраиваемыми результатами.
     */
    private class FakeValidationService extends ValidationService {

        /** Результат для {@link ValidationService#isValidName(String)}. */
        boolean validName = false;

        /** Результат для {@link ValidationService#isValidBio(String)}. */
        boolean validBio = false;

        @Override
        public boolean isValidName(String name) {
            return validName;
        }

        @Override
        public boolean isValidBio(String bio) {
            return validBio;
        }
    }
}