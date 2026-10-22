package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.Message;

import java.time.Duration;
import java.util.List;

/**
 * Команда /daily: выдаёт ежедневные карточки.
 */
public class DailyCommandHandler implements CommandHandler {

    /** Текст при активном кулдауне; подставляется оставшееся время. */
    private static final String COOLDOWN_TEMPLATE =
            "Ежедневная награда пока недоступна. Загляни через %s.";

    /** Поздравление; подставляется количество выпавших карточек. */
    private static final String CONGRATS_TEMPLATE =
            "🎁 Ежедневная награда! Тебе выпало карточек: %d";

    /** Шаблон карточки: эмодзи редкости, имя, редкость, статы. */
    private static final String CARD_TEMPLATE = """
            %s %s
            Редкость: %s
            ⚔️ Атака: %d
            🛡️ Защита: %d
            ❤️ Здоровье: %d
            """;

    /** Формат времени: часы и минуты. */
    private static final String HOURS_AND_MINUTES_FORMAT = "%d ч %d мин";

    /** Формат времени: только часы. */
    private static final String HOURS_ONLY_FORMAT = "%d ч";

    /** Формат времени: только минуты. */
    private static final String MINUTES_ONLY_FORMAT = "%d мин";

    /** Количество минут в часе. */
    private static final long MINUTES_IN_HOUR = 60L;

    /** Сервис карточек. */
    private final CardService cardService;

    /**
     * @param cardService сервис карточек
     */
    public DailyCommandHandler(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        List<Hero> heroes;
        try {
            heroes = cardService.grantDaily(context.userId());
        } catch (DailyCooldownException e) {
            String text = COOLDOWN_TEMPLATE.formatted(formatTimeLeft(e.timeLeft()));
            context.bot().sendMessage(new Message(text), context.chatId());
            return;
        }

        String congrats = CONGRATS_TEMPLATE.formatted(heroes.size());
        context.bot().sendMessage(new Message(congrats), context.chatId());

        for (Hero hero : heroes) {
            context.bot().sendMessage(new Message(formatCard(hero)), context.chatId());
        }
    }

    /**
     * Форматирует карточку героя для отправки пользователю.
     *
     * @param hero герой
     * @return текст сообщения
     */
    private String formatCard(Hero hero) {
        return CARD_TEMPLATE.formatted(
                hero.rarity().emoji(),
                hero.name(),
                hero.rarity().title(),
                hero.atk(),
                hero.def(),
                hero.hp()
        );
    }

    /**
     * Форматирует оставшееся время как «5 ч 32 мин». Неполная минута
     * округляется вверх, чтобы пользователь не пришёл на секунды раньше срока.
     *
     * @param timeLeft оставшееся время
     * @return читаемая строка
     */
    private String formatTimeLeft(Duration timeLeft) {
        long totalMinutes = Math.max(1L, timeLeft.plusMinutes(1).minusNanos(1).toMinutes());
        long hours = totalMinutes / MINUTES_IN_HOUR;
        long minutes = totalMinutes % MINUTES_IN_HOUR;

        if (hours == 0) {
            return MINUTES_ONLY_FORMAT.formatted(minutes);
        }
        if (minutes == 0) {
            return HOURS_ONLY_FORMAT.formatted(hours);
        }
        return HOURS_AND_MINUTES_FORMAT.formatted(hours, minutes);
    }
}
