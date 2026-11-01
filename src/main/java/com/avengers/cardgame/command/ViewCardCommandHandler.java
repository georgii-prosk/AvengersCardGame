package com.avengers.cardgame.command;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

import java.util.Optional;

/**
 * Обработчик команды /view_card: показывает детальную информацию о карточке героя.
 */
public class ViewCardCommandHandler implements CommandHandler {
    private final HeroRepository heroRepository;
    private final UserCardRepository userCardRepository;

    /** Рендерер для карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * @param heroRepository     репозиторий героев
     * @param userCardRepository репозиторий карточек пользователей
     */
    public ViewCardCommandHandler(HeroRepository heroRepository, UserCardRepository userCardRepository) {
        this.heroRepository = heroRepository;
        this.userCardRepository = userCardRepository;
    }

    @Override
    public void handle(CommandContext context) {
        long heroId;
        try {
            heroId = Long.parseLong(context.argument());
        } catch (NumberFormatException exception) {
            context.bot().sendMessage(new Message("Некорректный идентификатор карточки."), context.chatId());
            return;
        }
        Optional<Hero> hero = heroRepository.findById(heroId);
        if (hero.isEmpty()) {
            context.bot().sendMessage(new Message("Такой карточки не существует."), context.chatId());
            return;
        }
        int count = userCardRepository.findByUserId(context.userId()).stream()
                .filter(card -> card.heroId() == heroId)
                .mapToInt(UserCard::count).sum();
        if (count <= 0) {
            context.bot().sendMessage(new Message("Этой карточки нет в твоей коллекции."), context.chatId());
            return;
        }
        context.bot().sendMessage(new Message(heroRenderer.render(hero.get(), count)), context.chatId());
    }
}