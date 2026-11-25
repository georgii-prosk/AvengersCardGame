package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeCardService;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

class DailyCommandHandlerTest {

    private static final long USER_ID = 7L;
    private static final long CHAT_ID = 700L;

    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");
    private final Hero pepper = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "c");
    private final FakeBot bot = new FakeBot();

    private void runDaily(FakeCardService cardService) throws Exception {
        new DailyCommandHandler(cardService)
                .handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));
    }

    private String textOf(int index) {
        return bot.sent().get(index).message().text();
    }

    @Test
    @DisplayName("При успехе отправляет поздравление и по сообщению на каждую карточку")
    void successSendsCongratsAndOneMessagePerCard() throws Exception {
        FakeCardService service = new FakeCardService(List.of(ironMan, pepper));

        runDaily(service);

        Assertions.assertEquals(3, bot.count());
        Assertions.assertEquals("🎁 Ежедневная награда! Тебе выпало карточек: 2", textOf(0));
        Assertions.assertTrue(bot.sent().stream().allMatch(s -> s.chatId() == CHAT_ID));
        Assertions.assertEquals(USER_ID, service.lastUserId());
    }

    @Test
    @DisplayName("Карточки отправляются в порядке выдачи")
    void cardsAreSentInGrantedOrder() throws Exception {
        runDaily(new FakeCardService(List.of(ironMan, pepper)));

        Assertions.assertEquals(String.join("\n",
                "🟣 Iron Man",
                "Редкость: Эпическая",
                "⚔️ Атака: 80",
                "🛡️ Защита: 90",
                "❤️ Здоровье: 50",
                "e") + "\n", textOf(1));
        Assertions.assertEquals(String.join("\n",
                "⚪ Pepper Potts",
                "Редкость: Обычная",
                "⚔️ Атака: 5",
                "🛡️ Защита: 15",
                "❤️ Здоровье: 20",
                "c") + "\n", textOf(2));
    }

    @Test
    @DisplayName("При кулдауне отправляет одно сообщение с часами и минутами")
    void cooldownSendsSingleMessageWithHoursAndMinutes() throws Exception {
        runDaily(new FakeCardService(Duration.ofHours(5).plusMinutes(32)));

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals("Ежедневная награда пока недоступна. Загляни через 5 ч 32 мин.",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Неполная минута округляется вверх")
    void cooldownRoundsPartialMinuteUp() throws Exception {
        runDaily(new FakeCardService(Duration.ofHours(5).plusMinutes(31).plusSeconds(20)));

        Assertions.assertEquals("Ежедневная награда пока недоступна. Загляни через 5 ч 32 мин.",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Целые часы выводятся без минут")
    void cooldownWithWholeHoursShowsOnlyHours() throws Exception {
        runDaily(new FakeCardService(Duration.ofHours(2)));

        Assertions.assertEquals("Ежедневная награда пока недоступна. Загляни через 2 ч.",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Меньше минуты показывается как 1 мин")
    void cooldownUnderOneMinuteShowsOneMinute() throws Exception {
        runDaily(new FakeCardService(Duration.ofSeconds(20)));

        Assertions.assertEquals("Ежедневная награда пока недоступна. Загляни через 1 мин.",
                bot.lastSent().message().text());
    }
}
