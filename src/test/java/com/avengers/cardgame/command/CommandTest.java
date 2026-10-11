package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для команд бота.
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
                    .handle(new CommandRequest(1L, "Tony", ""));

            Assertions.assertTrue(reply.text().contains("AvengersCardGame"));
        }

        @Test
        @DisplayName("Не зависит от данных пользователя")
        void doesNotDependOnUserData() {
            Reply withName = new StartCommand()
                    .handle(new CommandRequest(1L, "Tony", ""));
            Reply withoutName = new StartCommand()
                    .handle(new CommandRequest(2L, null, ""));

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
                    .handle(new CommandRequest(1L, "Tony", ""));

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
                    .handle(new CommandRequest(1L, "Tony", ""));

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
                    .handle(new CommandRequest(1L, "Tony", ""));

            Assertions.assertTrue(reply.text().contains("не задано"));
        }

        @Test
        @DisplayName("Бросает UserNotFoundException, если пользователя нет")
        void shouldThrowWhenUserNotFound() {
            Assertions.assertThrows(UserNotFoundException.class,
                    () -> new ProfileCommand(userService)
                            .handle(new CommandRequest(1L, "Tony", "")));
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
                    .handle(new CommandRequest(1L, "Tony", "Narek"));

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
                    .handle(new CommandRequest(1L, "Tony", "bad name"));

            Assertions.assertEquals("OldName",
                    userRepository.findByUserId(1L).orElseThrow().displayName());
            Assertions.assertTrue(reply.text().contains("некорректное имя"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() {
            userRepository.save(new User(1L, "OldName"));

            Reply reply = new SetNameCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", ""));

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
                    .handle(new CommandRequest(1L, "Tony", "new bio"));

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
                    .handle(new CommandRequest(1L, "Tony", "bad bio"));

            Assertions.assertEquals("",
                    userRepository.findByUserId(1L).orElseThrow().bio());
            Assertions.assertTrue(reply.text().contains("некорректное описание"));
        }

        @Test
        @DisplayName("Обрабатывает пустой аргумент")
        void shouldHandleEmptyArgument() {
            userRepository.save(new User(1L, "Tony"));

            Reply reply = new SetBioCommand(userService, validationService)
                    .handle(new CommandRequest(1L, "Tony", ""));

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
            registry = new CommandRegistry();
            registry.register("/start", new StartCommand());
            registry.register("/help", new HelpCommand());
            registry.register("/profile", new ProfileCommand(userService));
            registry.register("/setname", new SetNameCommand(userService, validationService));
            registry.register("/setbio", new SetBioCommand(userService, validationService));
        }

        @Test
        @DisplayName("Разрешает /start без аргумента")
        void shouldResolveStartWithoutArgument() {
            Optional<ResolvedCommand> resolved = registry.resolve("/start");

            Assertions.assertTrue(resolved.isPresent());
            Assertions.assertInstanceOf(StartCommand.class, resolved.get().command());
            Assertions.assertEquals("", resolved.get().argument());
        }

        @Test
        @DisplayName("Разрешает /setname с аргументом")
        void shouldResolveSetNameWithArgument() {
            Optional<ResolvedCommand> resolved = registry.resolve("/setname Narek");

            Assertions.assertTrue(resolved.isPresent());
            Assertions.assertInstanceOf(SetNameCommand.class, resolved.get().command());
            Assertions.assertEquals("Narek", resolved.get().argument());
        }

        @Test
        @DisplayName("Разрешает /setbio с многословным аргументом")
        void shouldResolveSetBioWithMultiWordArgument() {
            Optional<ResolvedCommand> resolved = registry.resolve("/setbio Привет мир 🦸");

            Assertions.assertTrue(resolved.isPresent());
            Assertions.assertEquals("Привет мир 🦸", resolved.get().argument());
        }

        @Test
        @DisplayName("Регистронезависимость имени команды")
        void shouldResolveCaseInsensitive() {
            Assertions.assertTrue(registry.resolve("/START").isPresent());
            Assertions.assertTrue(registry.resolve("/SetName Narek").isPresent());
        }

        @Test
        @DisplayName("Пустой Optional для неизвестной команды")
        void shouldReturnEmptyForUnknown() {
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

        @Test
        @DisplayName("Команда без пробела после имени не распознаётся")
        void shouldNotMatchCommandWithoutSpace() {
            Assertions.assertTrue(registry.resolve("/setnameX foo").isEmpty());
        }

        @Test
        @DisplayName("Ведущие и хвостовые пробелы не мешают")
        void shouldTrimWhitespace() {
            Optional<ResolvedCommand> resolved = registry.resolve("   /setname Narek   ");

            Assertions.assertTrue(resolved.isPresent());
            Assertions.assertEquals("Narek", resolved.get().argument());
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