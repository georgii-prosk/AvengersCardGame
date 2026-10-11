package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для команд бота.
 * <p>
 * Команды предполагают, что пользователь уже зарегистрирован —
 * регистрацией занимается {@link RegisteringCommand}.
 */
class CommandTest {

    private FakeUserRepository userRepository;
    private UserService userService;
    private FakeValidationService validationService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
        validationService = new FakeValidationService();
    }

    @Nested
    @DisplayName("StartCommand")
    class StartCommandTest {

        @Test
        @DisplayName("Возвращает приветствие")
        void shouldReturnGreeting() {
            Reply reply = new StartCommand()
                    .handle(new CommandRequest(1L, "Tony", "/start"));

            Assertions.assertTrue(reply.text().contains("AvengersCardGame"));
        }

        @Test
        @DisplayName("Не зависит от данных пользователя")
        void doesNotDependOnUserData() {
            Reply withName = new StartCommand()
                    .handle(new CommandRequest(1L, "Tony", "/start"));
            Reply withoutName = new StartCommand()
                    .handle(new CommandRequest(2L, null, "/start"));

            Assertions.assertEquals(withName.text(), withoutName.text());
        }
    }

    @Nested
    @DisplayName("HelpCommand")
    class HelpCommandTest {

        @Test
        @DisplayName("Содержит все команды в ответе")
        void shouldContainAllCommands() {
            Reply reply = new HelpCommand()
                    .handle(new CommandRequest(1L, "Tony", "/help"));

            String text = reply.text();
            Assertions.assertTrue(text.contains("/start"));
            Assertions.assertTrue(text.contains("/help"));
            Assertions.assertTrue(text.contains("/profile"));
            Assertions.assertTrue(text.contains("/setname"));
            Assertions.assertTrue(text.contains("/setbio"));
        }
    }

    @Nested
    @DisplayName("ProfileCommand")
    class ProfileCommandTest {

        @Test
        @DisplayName("Содержит данные профиля")
        void shouldReturnProfileData() {
            userRepository.save(new User(1L, "Tony", "Genius", 100, 5, null, null));

            Reply reply = new ProfileCommand(userService)
                    .handle(new CommandRequest(1L, "Tony", "/profile"));

            String text = reply.text();
            Assertions.assertTrue(text.contains("Tony"));
            Assertions.assertTrue(text.contains("Genius"));
            Assertions.assertTrue(text.contains("100"));
            Assertions.assertTrue(text.contains("5"));
        }

        @Test
        @DisplayName("Показывает «не задано» для пустых полей")
        void shouldShowDefaultForEmptyFields() {
            userRepository.save(new User(1L, "", "", 0, 0, null, null));

            Reply reply = new ProfileCommand(userService)
                    .handle(new CommandRequest(1L, "Tony", "/profile"));

            Assertions.assertTrue(reply.text().contains("не задано"));
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void shouldThrowWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> new ProfileCommand(userService)
                            .handle(new CommandRequest(1L, "Tony", "/profile")));
        }
    }

    @Nested
    @DisplayName("SetNameCommand")
    class SetNameCommandTest {

        @Test
        @DisplayName("Обновляет имя при валидном вводе")
        void shouldUpdateNameWhenValid() {
            userRepository.save(new User(1L, "OldName"));
            validationService.validName = true;

            Reply reply = new SetNameCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setname Narek"));

            Assertions.assertEquals("Narek",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
            Assertions.assertTrue(reply.text().contains("Narek"));
        }

        @Test
        @DisplayName("Не обновляет при невалидном вводе")
        void shouldNotUpdateNameWhenInvalid() {
            userRepository.save(new User(1L, "OldName"));
            validationService.validName = false;

            Reply reply = new SetNameCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setname bad name"));

            Assertions.assertEquals("OldName",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
            Assertions.assertTrue(reply.text().contains("некорректное имя"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() {
            userRepository.save(new User(1L, "OldName"));

            Reply reply = new SetNameCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setname"));

            Assertions.assertEquals("OldName",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
            Assertions.assertTrue(reply.text().contains("некорректное имя"));
        }
    }

    @Nested
    @DisplayName("SetBioCommand")
    class SetBioCommandTest {

        @Test
        @DisplayName("Обновляет bio при валидном вводе")
        void shouldUpdateBioWhenValid() {
            userRepository.save(new User(1L, "Tony"));
            validationService.validBio = true;

            Reply reply = new SetBioCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setbio new bio"));

            Assertions.assertEquals("new bio",
                    userRepository.findByUserId(1L).orElseThrow().bio());
            Assertions.assertTrue(reply.text().contains("new bio"));
        }

        @Test
        @DisplayName("Не обновляет при невалидном вводе")
        void shouldNotUpdateBioWhenInvalid() {
            userRepository.save(new User(1L, "Tony"));
            validationService.validBio = false;

            Reply reply = new SetBioCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setbio bad bio"));

            Assertions.assertEquals("",
                    userRepository.findByUserId(1L).orElseThrow().bio());
            Assertions.assertTrue(reply.text().contains("некорректное описание"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() {
            userRepository.save(new User(1L, "Tony"));

            Reply reply = new SetBioCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", "/setbio"));

            Assertions.assertEquals("",
                    userRepository.findByUserId(1L).orElseThrow().bio());
            Assertions.assertTrue(reply.text().contains("некорректное описание"));
        }
    }

    @Nested
    @DisplayName("CommandRegistry")
    class CommandRegistryTest {

        private CommandRegistry registry;

        @BeforeEach
        void setUp() {
            registry = new CommandRegistry(userService, validationService);
        }

        @Test
        @DisplayName("Разрешает /start")
        void shouldResolveStart() {
            Assertions.assertTrue(registry.resolve("/start").isPresent());
        }

        @Test
        @DisplayName("Разрешает /help")
        void shouldResolveHelp() {
            Assertions.assertTrue(registry.resolve("/help").isPresent());
        }

        @Test
        @DisplayName("Разрешает /profile")
        void shouldResolveProfile() {
            Assertions.assertTrue(registry.resolve("/profile").isPresent());
        }

        @Test
        @DisplayName("Разрешает /setname")
        void shouldResolveSetName() {
            Assertions.assertTrue(registry.resolve("/setname").isPresent());
        }

        @Test
        @DisplayName("Разрешает /setbio")
        void shouldResolveSetBio() {
            Assertions.assertTrue(registry.resolve("/setbio").isPresent());
        }

        @Test
        @DisplayName("Регистронезависимость")
        void shouldResolveCaseInsensitive() {
            Assertions.assertTrue(registry.resolve("/START").isPresent());
        }

        @Test
        @DisplayName("Игнорирует аргументы")
        void shouldIgnoreArguments() {
            Assertions.assertTrue(registry.resolve("/setname Tony").isPresent());
        }

        @Test
        @DisplayName("Пустой Optional для неизвестной команды")
        void shouldReturnEmptyForUnknownCommand() {
            Assertions.assertTrue(registry.resolve("/foo").isEmpty());
        }

        @Test
        @DisplayName("Пустой Optional для null")
        void shouldReturnEmptyForNull() {
            Assertions.assertTrue(registry.resolve(null).isEmpty());
        }

        @Test
        @DisplayName("Пустой Optional для пробелов")
        void shouldReturnEmptyForBlank() {
            Assertions.assertTrue(registry.resolve("   ").isEmpty());
        }
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

    private class FakeValidationService extends ValidationService {

        boolean validName = false;
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