package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Обработчик кнопки просмотра карточки ({@code view_card:<heroId>}).
 */
public class ViewCardCallback implements CallbackHandler {

    /** Шаблон карточки: эмодзи редкости, имя, редкость, статы, описание, количество. */
    private static final String CARD_TEMPLATE = """
            %s %s
            Редкость: %s
            ⚔️ Атака: %d
            🛡️ Защита: %d
            ❤️ Здоровье: %d
            %sВ коллекции: ×%d
            """;

    /** Текст, если героя не существует. */
    private static final String HERO_NOT_FOUND_MESSAGE = "Такой карточки не существует.";

    /** Текст, если у пользователя нет этой карточки. */
    private static final String NOT_OWNED_MESSAGE = "Этой карточки нет в твоей коллекции.";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(ViewCardCallback.class);

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /**
     * @param heroRepository     репозиторий героев
     * @param userCardRepository репозиторий карточек пользователей
     */
    public ViewCardCallback(HeroRepository heroRepository, UserCardRepository userCardRepository) {
        this.heroRepository = heroRepository;
        this.userCardRepository = userCardRepository;
    }

    @Override
    public void handle(CallbackContext context) throws BotException {
        long heroId;
        try {
            heroId = Long.parseLong(context.argument());
        } catch (NumberFormatException e) {
            log.warn("Invalid hero id in callback data: {}", context.callbackData());
            return;
        }

        Optional<Hero> hero = heroRepository.findById(heroId);
        if (hero.isEmpty()) {
            context.bot().sendMessage(new Message(HERO_NOT_FOUND_MESSAGE), context.chatId());
            return;
        }

        int count = countCopies(context.userId(), heroId);
        if (count == 0) {
            context.bot().sendMessage(new Message(NOT_OWNED_MESSAGE), context.chatId());
            return;
        }

        context.bot().sendMessage(new Message(formatCard(hero.get(), count)), context.chatId());
    }

    /**
     * Считает копии карточки героя у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    private int countCopies(long userId, long heroId) {
        return userCardRepository.findByUserId(userId).stream()
                .filter(card -> card.heroId() == heroId)
                .mapToInt(UserCard::count)
                .sum();
    }

    /**
     * Форматирует карточку со статами и количеством копий.
     *
     * @param hero  герой
     * @param count количество копий у пользователя
     * @return текст сообщения
     */
    private String formatCard(Hero hero, int count) {
        String description = hero.description() == null || hero.description().isBlank()
                ? ""
                : hero.description() + "\n";
        return CARD_TEMPLATE.formatted(
                hero.rarity().emoji(),
                hero.name(),
                hero.rarity().title(),
                hero.atk(),
                hero.def(),
                hero.hp(),
                description,
                count
        );
    }
}
