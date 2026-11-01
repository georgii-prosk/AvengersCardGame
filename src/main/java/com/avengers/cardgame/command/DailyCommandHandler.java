package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.messaging.Message;

import java.time.Duration;
import java.util.List;

/**
 * Обработчик команды /daily: выдает ежедневные карточки или сообщает о кулдауне.
 */
public class DailyCommandHandler implements CommandHandler {
    /** Шаблон сообщения при активном кулдауне; подставляется оставшееся время. */
    private static final String COOLDOWN_TEMPLATE = "Ежедневная награда пока недоступна. Загляни через %s.";

    /** Шаблон поздравления с получением награды; подставляется количество выпавших карточек. */
    private static final String CONGRATS_TEMPLATE = "🎁 Ежедневная награда! Тебе выпало карточек: %d";

    /** Количество минут в одном часе. */
    private static final long MINUTES_IN_HOUR = 60;

    /** Сервис для выдачи карточек. */
    private final CardService cardService;

    /** Рендерер для форматирования текста карточек. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * @param cardService сервис для выдачи карточек
     */
    public DailyCommandHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        DailyRewardResult result = cardService.grantDaily(context.userId());

        if (!result.claimed()) {
            context.bot().sendMessage(new Message(COOLDOWN_TEMPLATE.formatted(
                    formatTimeLeft(result.timeUntilNextClaim()))), context.chatId());
            return;
        }

        List<Hero> heroes = result.heroes();
        context.bot().sendMessage(new Message(CONGRATS_TEMPLATE.formatted(heroes.size())), context.chatId());

        for (Hero hero : heroes) {
            context.bot().sendMessage(new Message(heroRenderer.render(hero)), context.chatId());
        }
    }

    /**
     * Форматирует оставшееся время в читаемый вид (часы и минуты).
     * Неполная минута округляется вверх.
     *
     * @param timeLeft оставшееся время
     * @return строка с оставшимся временем (например, "5 ч 32 мин")
     */
    private String formatTimeLeft(Duration timeLeft) {
        long minutes = Math.max(1, timeLeft.plusMinutes(1).minusNanos(1).toMinutes());
        long hours = minutes / MINUTES_IN_HOUR;
        long remainingMinutes = minutes % MINUTES_IN_HOUR;

        if (hours == 0) return remainingMinutes + " мин";
        if (remainingMinutes == 0) return hours + " ч";
        return hours + " ч " + remainingMinutes + " мин";
    }
}