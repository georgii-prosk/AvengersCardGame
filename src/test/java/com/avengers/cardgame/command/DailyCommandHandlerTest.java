package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

/**
 * Тесты для {@link DailyCommandHandler}.
 */
class DailyCommandHandlerTest {

    private static final long USER_ID = 7L;
    private static final long CHAT_ID = 700L;

    /** Эпический герой. */
    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Обычный герой. */
    private final Hero pepper = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Бот, собирающий сообщения. */
    private final FakeBot bot = new FakeBot();

    /**
     * Запускает команду /daily с указанным сервисом.
     *
     * @param cardService подставной сервис карточек
     * @throws Exception если команда завершилась ошибкой
     */
    private void runDaily(FakeCardService cardService) throws Exception {
        new DailyCommandHandler(cardService).handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));
    }

    /**
     * @param index номер сообщения
     * @return текст сообщения
     */
    private String textOf(int index) {
        return bot.sent().get(index).message().text();
    }

    @Test
    @DisplayName("При успехе отправляет поздравление и по сообщению на каждую карточку")
    void successSendsCongratsAndOneMessagePerCard() throws Exception {
        FakeCardService service = new FakeCardService(List.of(ironMan, pepper));

        runDaily(service);

        Assertions.assertEquals(3, bot.count());
        Assertions.assertTrue(textOf(0).contains("2"));
        Assertions.assertTrue(bot.sent().stream().allMatch(s -> s.chatId() == CHAT_ID));
        Assertions.assertEquals(USER_ID, service.lastUserId());
    }

    @Test
    @DisplayName("Сообщение о карточке содержит эмодзи, имя, редкость и статы")
    void cardMessageContainsEmojiNameRarityAndStats() throws Exception {
        runDaily(new FakeCardService(List.of(ironMan)));

        String card = textOf(1);
        Assertions.assertTrue(card.contains(HeroRarity.EPIC.emoji()));
        Assertions.assertTrue(card.contains("Iron Man"));
        Assertions.assertTrue(card.contains(HeroRarity.EPIC.title()));
        Assertions.assertTrue(card.contains("80"));
        Assertions.assertTrue(card.contains("90"));
        Assertions.assertTrue(card.contains("50"));
    }

    @Test
    @DisplayName("Карточки отправляются в порядке выдачи")
    void cardsAreSentInGrantedOrder() throws Exception {
        runDaily(new FakeCardService(List.of(ironMan, pepper)));

        Assertions.assertTrue(textOf(1).contains("Iron Man"));
        Assertions.assertTrue(textOf(2).contains("Pepper Potts"));
    }

    @Test
    @DisplayName("При кулдауне отправляет одно сообщение с часами и минутами")
    void cooldownSendsSingleMessageWithHoursAndMinutes() throws Exception {
        Duration left = Duration.ofHours(5).plusMinutes(32);

        runDaily(new FakeCardService(new DailyCooldownException(left)));

        Assertions.assertEquals(1, bot.count());
        Assertions.assertTrue(bot.lastSent().message().text().contains("5 ч 32 мин"));
    }

    @Test
    @DisplayName("Неполная минута округляется вверх")
    void cooldownRoundsPartialMinuteUp() throws Exception {
        Duration left = Duration.ofHours(5).plusMinutes(31).plusSeconds(20);

        runDaily(new FakeCardService(new DailyCooldownException(left)));

        Assertions.assertTrue(bot.lastSent().message().text().contains("5 ч 32 мин"));
    }

    @Test
    @DisplayName("Целые часы выводятся без минут")
    void cooldownWithWholeHoursShowsOnlyHours() throws Exception {
        runDaily(new FakeCardService(new DailyCooldownException(Duration.ofHours(2))));

        Assertions.assertTrue(bot.lastSent().message().text().contains("2 ч"));
        Assertions.assertFalse(bot.lastSent().message().text().contains("мин"));
    }

    @Test
    @DisplayName("Меньше минуты показывается как 1 мин")
    void cooldownUnderOneMinuteShowsOneMinute() throws Exception {
        runDaily(new FakeCardService(new DailyCooldownException(Duration.ofSeconds(20))));

        Assertions.assertTrue(bot.lastSent().message().text().contains("1 мин"));
    }
}
