package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Тесты для {@link CraftMakeCommandHandler}.
 */
class CraftMakeCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Фейковый сервис карточек. */
    private FakeCardService cardService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private CraftMakeCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardService = new FakeCardService(List.of(commonHero));
        bot = new FakeBot();
        handler = new CraftMakeCommandHandler(cardService);
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
     * Проверяет успешный крафт.
     */
    @Test
    @DisplayName("Сообщает об успешном крафте")
    void craftsCard() throws Exception {
        handler.handle(context("common"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Крафт успешен"));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertTrue(message.text().contains("Потрачено кусочков: 5"));
        Assertions.assertTrue(message.text().contains("Остаток кусочков: 0"));
    }

    /**
     * Проверяет обработку нехватки кусочков.
     */
    @Test
    @DisplayName("Обрабатывает нехватку кусочков")
    void handlesInsufficientShards() throws Exception {
        cardService.craftThrows(new InsufficientShardsException(100, 15));

        handler.handle(context("legendary"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Недостаточно кусочков"));
        Assertions.assertTrue(message.text().contains("Нужно: 100"));
        Assertions.assertTrue(message.text().contains("у тебя: 15"));
        Assertions.assertTrue(message.text().contains("Не хватает: 85"));
    }
}
