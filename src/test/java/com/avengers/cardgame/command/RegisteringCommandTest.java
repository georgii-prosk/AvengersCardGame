package com.avengers.cardgame.command;

import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Тесты для {@link RegisteringCommand}.
 */
class RegisteringCommandTest {

    private FakeUserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("Регистрирует нового пользователя перед вызовом делегата")
    void registersNewUserBeforeDelegate() {
        Command delegate = request -> new Reply("ok");
        Command registering = new RegisteringCommand(delegate, userService);

        registering.handle(new CommandRequest(1L, "Tony", "/anything"));

        Assertions.assertTrue(userRepository.findByUserId(1L).isPresent());
        Assertions.assertEquals("Tony",
                userRepository.findByUserId(1L).orElseThrow().displayName());
    }

    @Test
    @DisplayName("Использует Avenger при отсутствии username")
    void usesDefaultNameWhenUsernameMissing() {
        Command delegate = request -> new Reply("ok");
        Command registering = new RegisteringCommand(delegate, userService);

        registering.handle(new CommandRequest(1L, null, "/anything"));

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(1L).orElseThrow().displayName());
    }

    @Test
    @DisplayName("Использует Avenger при пустом username")
    void usesDefaultNameWhenUsernameBlank() {
        Command delegate = request -> new Reply("ok");
        Command registering = new RegisteringCommand(delegate, userService);

        registering.handle(new CommandRequest(1L, "   ", "/anything"));

        Assertions.assertEquals("Avenger",
                userRepository.findByUserId(1L).orElseThrow().displayName());
    }

    @Test
    @DisplayName("Не перезаписывает существующего пользователя")
    void doesNotOverwriteExistingUser() {
        userRepository.save(new User(1L, "Original"));

        Command delegate = request -> new Reply("ok");
        Command registering = new RegisteringCommand(delegate, userService);

        registering.handle(new CommandRequest(1L, "Tony", "/anything"));

        Assertions.assertEquals("Original",
                userRepository.findByUserId(1L).orElseThrow().displayName());
    }

    @Test
    @DisplayName("Возвращает ответ делегата")
    void returnsDelegateReply() {
        Command delegate = request -> new Reply("delegate answer");
        Command registering = new RegisteringCommand(delegate, userService);

        Reply reply = registering.handle(new CommandRequest(1L, "Tony", "/anything"));

        Assertions.assertEquals("delegate answer", reply.text());
    }

    @Test
    @DisplayName("Передаёт исходный CommandRequest делегату")
    void passesRequestToDelegate() {
        CommandRequest[] captured = new CommandRequest[1];
        Command delegate = request -> {
            captured[0] = request;
            return new Reply("ok");
        };
        Command registering = new RegisteringCommand(delegate, userService);

        CommandRequest original = new CommandRequest(42L, "Tony", "/anything now");
        registering.handle(original);

        Assertions.assertEquals(42L, captured[0].userId());
        Assertions.assertEquals("Tony", captured[0].userName());
        Assertions.assertEquals("/anything now", captured[0].text());
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