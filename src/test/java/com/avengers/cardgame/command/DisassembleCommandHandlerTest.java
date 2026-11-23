package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
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

import java.time.Duration;

/**
 * Тесты для {@link DisassembleCommandHandler}.
 */
class DisassembleCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig gameConfig = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private DisassembleCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(commonHero);
        bot = new FakeBot();
        handler = new DisassembleCommandHandler(cardRepository, heroRepository, gameConfig);
    }

    /**
     * Создаёт контекст команды.
     *
     * @return контекст
     */
    private CommandContext context() {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot);
    }

    /**
     * Проверяет пустое сообщение при отсутствии карточек.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточек")
    void sendsEmptyMessage() {
        handler.handle(context());

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }

    /**
     * Проверяет отображение коллекции для разбора.
     */
    @Test
    @DisplayName("Показывает карточки для разбора")
    void rendersSelection() {
        cardRepository.addCard(USER_ID, commonHero.id());
        cardRepository.addCard(USER_ID, commonHero.id());

        handler.handle(context());

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("страница 1/1"));
        Assertions.assertEquals("/disassemble_pick 1",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет, что выбирается только своя карточка.
     */
    @Test
    @DisplayName("Считает только карточки пользователя")
    void ignoresForeignCards() {
        cardRepository.addCard(2L, commonHero.id());

        handler.handle(context());

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }
}
