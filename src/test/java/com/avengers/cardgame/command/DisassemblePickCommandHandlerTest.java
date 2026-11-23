package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link DisassemblePickCommandHandler}.
 */
class DisassemblePickCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private DisassemblePickCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        bot = new FakeBot();
        handler = new DisassemblePickCommandHandler(cardRepository, heroRepository);
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
     * Проверяет показ вариантов количества.
     */
    @Test
    @DisplayName("Показывает варианты количества")
    void showsQuantityOptions() {
        for (int index = 0; index < 7; index++) {
            cardRepository.addCard(USER_ID, commonHero.id());
        }

        handler.handle(context("1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("×7"));
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/disassemble_qty 1 all",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет некорректный идентификатор карточки.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе")
    void rejectsInvalidHeroId() {
        handler.handle(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный идентификатор"));
    }

    /**
     * Проверяет отсутствующего героя.
     */
    @Test
    @DisplayName("Сообщает об отсутствующем герое")
    void rejectsUnknownHero() {
        handler.handle(context("999"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("не существует"));
    }

    /**
     * Проверяет карточку, которой нет в коллекции.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточки в коллекции")
    void rejectsMissingCard() {
        handler.handle(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет в твоей коллекции"));
    }
}
