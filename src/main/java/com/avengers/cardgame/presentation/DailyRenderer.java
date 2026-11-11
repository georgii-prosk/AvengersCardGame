package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Формирует сообщения ежедневной награды.
 */
public class DailyRenderer {

    /** Шаблон сообщения при активном кулдауне; подставляется оставшееся время. */
    private static final String COOLDOWN_TEMPLATE = "Ежедневная награда пока недоступна. Загляни через %s.";

    /** Шаблон поздравления с получением награды; подставляется количество выпавших карточек. */
    private static final String CONGRATS_TEMPLATE = "🎁 Ежедневная награда! Тебе выпало карточек: %d";

    /** Рендерер карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /** Форматтер промежутков времени. */
    private final DurationFormatter durationFormatter = new DurationFormatter();

    /**
     * Формирует сообщение о недоступной ежедневной награде.
     *
     * @param timeLeft оставшееся время до следующей награды
     * @return сообщение о кулдауне
     */
    public Message renderCooldown(@NotNull Duration timeLeft) {
        return new Message(COOLDOWN_TEMPLATE.formatted(durationFormatter.format(timeLeft)));
    }

    /**
     * Формирует поздравление с получением ежедневной награды.
     *
     * @param cardCount количество выпавших карточек
     * @return сообщение с поздравлением
     */
    public Message renderCongrats(int cardCount) {
        return new Message(CONGRATS_TEMPLATE.formatted(cardCount));
    }

    /**
     * Формирует сообщения с выданными карточками.
     *
     * @param heroes выданные герои
     * @return список сообщений — по одному на каждую карточку, в порядке выдачи
     */
    public List<Message> renderCards(@NotNull List<Hero> heroes) {
        List<Message> messages = new ArrayList<>(heroes.size());
        for (Hero hero : heroes) {
            messages.add(new Message(heroRenderer.render(hero)));
        }
        return messages;
    }
}
