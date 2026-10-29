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

public class DailyCommandHandler implements CommandHandler {
    private static final String COOLDOWN_TEMPLATE = "Ежедневная награда пока недоступна. Загляни через %s.";
    private static final String CONGRATS_TEMPLATE = "🎁 Ежедневная награда! Тебе выпало карточек: %d";
    private static final long MINUTES_IN_HOUR = 60;
    private final CardService cardService;
    private final HeroRenderer heroRenderer;

    public DailyCommandHandler(CardService cardService, HeroRenderer heroRenderer) {
        this.cardService = cardService;
        this.heroRenderer = heroRenderer;
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

    private String formatTimeLeft(Duration timeLeft) {
        long minutes = Math.max(1, timeLeft.plusMinutes(1).minusNanos(1).toMinutes());
        long hours = minutes / MINUTES_IN_HOUR;
        long remainingMinutes = minutes % MINUTES_IN_HOUR;
        if (hours == 0) return remainingMinutes + " мин";
        if (remainingMinutes == 0) return hours + " ч";
        return hours + " ч " + remainingMinutes + " мин";
    }
}
