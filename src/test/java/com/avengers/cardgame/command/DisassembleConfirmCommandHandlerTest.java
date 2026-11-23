package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

/**
 * Тесты для {@link DisassembleConfirmCommandHandler}.
 */
class DisassembleConfirmCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private DisassembleConfirmCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        cardService = new FakeCardService(List.of(commonHero));
        bot = new FakeBot();
        handler = new DisassembleConfirmCommandHandler(cardService, heroRepository, gameConfig);
    }

    /**
     * Создаёт контекст команды.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", argument, bot);
    }

    /**
     * Проверяет успешный разбор.
     */
    @Test
    @DisplayName("Сообщает об успешном разборе")
    void confirmsDisassemble() throws Exception {
        handler.handle(context("1 2"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Разобрано"));
        Assertions.assertTrue(message.text().contains("×2"));
        Assertions.assertTrue(message.text().contains("Получено кусочков: 2"));
    }

    /**
     * Проверяет обработку ошибки нехватки карточек.
     */
    @Test
    @DisplayName("Обрабатывает ошибку нехватки карточек")
    void handlesNotEnoughCards() throws Exception {
        cardService.disassembleThrows(new NotEnoughCardsException(1L, 5, 2));

        handler.handle(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("×2"));
    }

    /**
     * Проверяет некорректные параметры подтверждения.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры подтверждения")
    void rejectsInvalidConfirmation() throws Exception {
        handler.handle(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректное количество"));
    }
}
