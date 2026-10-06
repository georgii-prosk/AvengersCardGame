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
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для команд Telegram-бота.
 * <p>
 * Используются ручные фейковые реализации {@link TelegramClient},
 * {@link UserRepository} и {@link ValidationService} — без Mockito.
 */
class CommandTest {

    /** Фейковый клиент Telegram, запоминающий отправленные сообщения. */
    private FakeTelegramClient client;

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
        client = new FakeTelegramClient();
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
        validationService = new FakeValidationService();
    }

    /**
     * Тесты для {@link StartCommand}.
     */
    @Nested
    @DisplayName("StartCommand")
    class StartCommandTest {

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /start")
        void shouldReturnName() {
            Assertions.assertEquals("/start", new StartCommand(userService).name());
        }

        /**
         * Проверяет регистрацию пользователя с переданным username
         * и отправку приветствия.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Регистрирует пользователя и отправляет приветствие")
        void shouldRegisterAndSendGreeting() throws TelegramApiException {
            new StartCommand(userService).execute(client, 10L, 1L, "Tony", "/start");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isPresent());
            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
            Assertions.assertTrue(client.lastText().contains("AvengersCardGame"));
        }

        /**
         * Проверяет использование имени по умолчанию при отсутствии username.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Использует Avenger при отсутствии username")
        void shouldUseDefaultNameWhenUsernameMissing() throws TelegramApiException {
            new StartCommand(userService).execute(client, 10L, 1L, null, "/start");

            Assertions.assertEquals("Avenger",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
        }

        /**
         * Проверяет, что повторный вызов не перезаписывает существующего
         * пользователя.
         *
         * @throws TelegramApiException при ошибке отправки
         */
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

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /help")
        void shouldReturnName() {
            Assertions.assertEquals("/help", new HelpCommand().name());
        }

        /**
         * Проверяет, что сообщение содержит список всех команд.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Отправляет список команд")
        void shouldSendHelpMessage() throws TelegramApiException {
            new HelpCommand().execute(client, 10L, 1L, "Tony", "/help");

            String text = client.lastText();
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

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /profile")
        void shouldReturnName() {
            Assertions.assertEquals("/profile", new ProfileCommand(userService).name());
        }

        /**
         * Проверяет вывод данных профиля.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Отправляет данные профиля")
        void shouldSendProfileData() throws TelegramApiException {
            userRepository.save(new User(1L, "Tony", "Genius", 100, 5, null, null));

            new ProfileCommand(userService).execute(client, 10L, 1L, "Tony", "/profile");

            String text = client.lastText();
            Assertions.assertTrue(text.contains("Tony"));
            Assertions.assertTrue(text.contains("Genius"));
            Assertions.assertTrue(text.contains("100"));
            Assertions.assertTrue(text.contains("5"));
        }

        /**
         * Проверяет отображение «не задано» для пустых имени и bio.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Отображает «не задано» для пустых полей")
        void shouldShowDefaultForEmptyFields() throws TelegramApiException {
            userRepository.save(new User(1L, "", "", 0, 0, null, null));

            new ProfileCommand(userService).execute(client, 10L, 1L, "Tony", "/profile");

            Assertions.assertTrue(client.lastText().contains("не задано"));
        }

        /**
         * Проверяет авторегистрацию пользователя при отсутствии.
         *
         * @throws TelegramApiException при ошибке отправки
         */
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

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /setname")
        void shouldReturnName() {
            Assertions.assertEquals("/setname",
                    new SetNameCommand(userService, validationService).name());
        }

        /**
         * Проверяет успешное обновление имени при валидном вводе.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Обновляет имя при корректном вводе")
        void shouldUpdateNameWhenValid() throws TelegramApiException {
            validationService.validName = true;

            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname Tony");

            Assertions.assertEquals("Tony",
                    userRepository.findByTelegramId(1L).orElseThrow().getDisplayName());
            Assertions.assertTrue(client.lastText().contains("Tony"));
        }

        /**
         * Проверяет, что при невалидном имени обновление не выполняется.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Не обновляет имя при невалидном вводе")
        void shouldNotUpdateNameWhenInvalid() throws TelegramApiException {
            validationService.validName = false;

            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname bad name");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(client.lastText().contains("некорректное имя"));
        }

        /**
         * Проверяет обработку пустого аргумента.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() throws TelegramApiException {
            new SetNameCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setname");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(client.lastText().contains("некорректное имя"));
        }
    }

    /**
     * Тесты для {@link SetBioCommand}.
     */
    @Nested
    @DisplayName("SetBioCommand")
    class SetBioCommandTest {

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /setbio")
        void shouldReturnName() {
            Assertions.assertEquals("/setbio",
                    new SetBioCommand(userService, validationService).name());
        }

        /**
         * Проверяет успешное обновление bio при валидном вводе.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Обновляет bio при корректном вводе")
        void shouldUpdateBioWhenValid() throws TelegramApiException {
            validationService.validBio = true;

            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio new bio");

            Assertions.assertEquals("new bio",
                    userRepository.findByTelegramId(1L).orElseThrow().getBio());
            Assertions.assertTrue(client.lastText().contains("new bio"));
        }

        /**
         * Проверяет, что при невалидном bio обновление не выполняется.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Не обновляет bio при невалидном вводе")
        void shouldNotUpdateBioWhenInvalid() throws TelegramApiException {
            validationService.validBio = false;

            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio bad bio");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(client.lastText().contains("некорректное описание"));
        }

        /**
         * Проверяет обработку пустого аргумента.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() throws TelegramApiException {
            new SetBioCommand(userService, validationService)
                    .execute(client, 10L, 1L, "Tony", "/setbio");

            Assertions.assertTrue(userRepository.findByTelegramId(1L).isEmpty());
            Assertions.assertTrue(client.lastText().contains("некорректное описание"));
        }
    }

    /**
     * Тесты для {@link UnknownCommand}.
     */
    @Nested
    @DisplayName("UnknownCommand")
    class UnknownCommandTest {

        /**
         * Проверяет имя команды.
         */
        @Test
        @DisplayName("Возвращает имя /unknown")
        void shouldReturnName() {
            Assertions.assertEquals("/unknown", new UnknownCommand().name());
        }

        /**
         * Проверяет отправку сообщения о неизвестной команде.
         *
         * @throws TelegramApiException при ошибке отправки
         */
        @Test
        @DisplayName("Отправляет сообщение о неизвестной команде")
        void shouldSendUnknownMessage() throws TelegramApiException {
            new UnknownCommand().execute(client, 10L, 1L, "Tony", "/foo");

            String text = client.lastText();
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

        /**
         * Создаёт реестр перед каждым тестом.
         */
        @BeforeEach
        void setUp() {
            registry = new CommandRegistry(userService, validationService);
        }

        /**
         * Проверяет разрешение {@code /start}.
         */
        @Test
        @DisplayName("Разрешает /start")
        void shouldResolveStart() {
            Assertions.assertInstanceOf(StartCommand.class, registry.resolve("/start"));
        }

        /**
         * Проверяет разрешение {@code /help}.
         */
        @Test
        @DisplayName("Разрешает /help")
        void shouldResolveHelp() {
            Assertions.assertInstanceOf(HelpCommand.class, registry.resolve("/help"));
        }

        /**
         * Проверяет разрешение {@code /profile}.
         */
        @Test
        @DisplayName("Разрешает /profile")
        void shouldResolveProfile() {
            Assertions.assertInstanceOf(ProfileCommand.class, registry.resolve("/profile"));
        }

        /**
         * Проверяет разрешение {@code /setname}.
         */
        @Test
        @DisplayName("Разрешает /setname")
        void shouldResolveSetName() {
            Assertions.assertInstanceOf(SetNameCommand.class, registry.resolve("/setname"));
        }

        /**
         * Проверяет разрешение {@code /setbio}.
         */
        @Test
        @DisplayName("Разрешает /setbio")
        void shouldResolveSetBio() {
            Assertions.assertInstanceOf(SetBioCommand.class, registry.resolve("/setbio"));
        }

        /**
         * Проверяет регистронезависимость.
         */
        @Test
        @DisplayName("Разрешает команду в любом регистре")
        void shouldResolveCaseInsensitive() {
            Assertions.assertInstanceOf(StartCommand.class, registry.resolve("/START"));
        }

        /**
         * Проверяет игнорирование аргументов при разрешении.
         */
        @Test
        @DisplayName("Игнорирует аргументы команды")
        void shouldIgnoreArguments() {
            Assertions.assertInstanceOf(SetNameCommand.class,
                    registry.resolve("/setname Tony"));
        }

        /**
         * Проверяет неизвестную команду.
         */
        @Test
        @DisplayName("Возвращает UnknownCommand для неизвестной команды")
        void shouldReturnUnknownForUnknownCommand() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve("/foo"));
        }

        /**
         * Проверяет {@code null}.
         */
        @Test
        @DisplayName("Возвращает UnknownCommand для null")
        void shouldReturnUnknownForNull() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve(null));
        }

        /**
         * Проверяет пустую строку.
         */
        @Test
        @DisplayName("Возвращает UnknownCommand для пустой строки")
        void shouldResolveBlankAsUnknown() {
            Assertions.assertInstanceOf(UnknownCommand.class, registry.resolve("   "));
        }
    }

    /**
     * Фейковый клиент Telegram для версии библиотеки 7.10.0.
     * <p>
     * Сохраняет тексты отправленных сообщений, не выполняя реальных
     * запросов к API.
     */
    private static class FakeTelegramClient implements TelegramClient {

        /** Список отправленных текстов. */
        private final List<String> sentTexts = new ArrayList<>();

        /**
         * {@inheritDoc}
         * <p>
         * Если передан {@link SendMessage}, сохраняет его текст.
         *
         * @param method метод Bot API
         * @param <T>    тип результата
         * @param <M>    тип метода
         * @return {@code null}
         * @throws TelegramApiException не выбрасывается
         */
        @Override
        public <T extends Serializable, M extends BotApiMethod<T>>
        T execute(M method) throws TelegramApiException {
            if (method instanceof SendMessage sendMessage) {
                sentTexts.add(sendMessage.getText());
            }
            return null;
        }

        /**
         * Возвращает текст последнего отправленного сообщения.
         *
         * @return текст последнего сообщения
         */
        String lastText() {
            Assertions.assertFalse(sentTexts.isEmpty(), "Сообщение не было отправлено");
            return sentTexts.get(sentTexts.size() - 1);
        }
    }

    /**
     * Фейковый репозиторий пользователей в памяти.
     */
    private static class FakeUserRepository implements UserRepository {

        /** Хранилище пользователей по Telegram ID. */
        private final Map<Long, User> storage = new HashMap<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<User> findByTelegramId(long telegramId) {
            return Optional.ofNullable(storage.get(telegramId));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void save(User user) {
            storage.put(user.getTelegramId(), user);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(User user) {
            storage.put(user.getTelegramId(), user);
        }
    }

    /**
     * Фейковый сервис валидации с настраиваемыми результатами.
     */
    private static class FakeValidationService extends ValidationService {

        /** Результат для {@link ValidationService#isValidName(String)}. */
        boolean validName = false;

        /** Результат для {@link ValidationService#isValidBio(String)}. */
        boolean validBio = false;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValidName(String name) {
            return validName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isValidBio(String bio) {
            return validBio;
        }
    }
}