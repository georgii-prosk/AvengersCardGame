package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link SellPageCommandHandler}.
 */
class SellPageCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellPageCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryUserCardRepository cardRepository = new InMemoryUserCardRepository(new InMemoryUserRepository());
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(COMMON_HERO);
        bot = new FakeBot();
        handler = new SellPageCommandHandler(cardRepository, heroRepository);
    }

    /**
     * Создаёт контекст команды.
     *
     * @return контекст
     */
    private CommandContext context() {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", "abc", bot);
    }

    /**
     * Проверяет некорректный номер страницы.
     */
    @Test
    @DisplayName("Сообщает о некорректной странице")
    void rejectsInvalidPage() {
        handler.handle(context());

        Assertions.assertEquals("Некорректный номер страницы.",
                bot.lastSent().message().text());
    }
}
