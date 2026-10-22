package com.avengers.cardgame.command;

import com.avengers.cardgame.command.callback.CallbackPrefix;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.service.Button;
import com.avengers.cardgame.service.Message;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Формирует страницу инвентаря (текст и кнопки).
 */
public class InventoryRenderer {

    /** Текст для пустой коллекции. */
    private static final String EMPTY_MESSAGE =
            "У тебя пока нет карточек. Забери ежедневную награду командой /daily!";

    /** Заголовок страницы; подставляются номер страницы и их количество. */
    private static final String HEADER_TEMPLATE = "Твоя коллекция (страница %d/%d):";

    /** Строка карточки в списке: эмодзи, имя, редкость, количество. */
    private static final String LINE_TEMPLATE = "%s %s — %s ×%d";

    /** Надпись на кнопке карточки: эмодзи, имя, количество. */
    private static final String CARD_BUTTON_TEMPLATE = "%s %s ×%d";

    /** Надпись кнопки «назад». */
    private static final String PREVIOUS_BUTTON_TEXT = "◀️ Назад";

    /** Надпись кнопки «вперёд». */
    private static final String NEXT_BUTTON_TEXT = "Вперёд ▶️";

    /** Количество карточек на одной странице. */
    private static final int PAGE_SIZE = 5;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /**
     * @param userCardRepository репозиторий карточек пользователей
     * @param heroRepository     репозиторий героев
     */
    public InventoryRenderer(UserCardRepository userCardRepository, HeroRepository heroRepository) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
    }

    /**
     * Формирует сообщение с указанной страницей инвентаря. Номер страницы
     * за пределами диапазона приводится к ближайшей существующей странице.
     *
     * @param userId идентификатор пользователя
     * @param page   номер страницы, начиная с 1
     * @return сообщение с текстом и кнопками
     */
    public Message render(long userId, int page) {
        List<Entry> entries = loadEntries(userId);
        if (entries.isEmpty()) {
            return new Message(EMPTY_MESSAGE);
        }

        int totalPages = (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int from = (currentPage - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, entries.size());
        List<Entry> pageEntries = entries.subList(from, to);

        StringBuilder text = new StringBuilder(HEADER_TEMPLATE.formatted(currentPage, totalPages));
        List<List<Button>> rows = new ArrayList<>();
        for (Entry entry : pageEntries) {
            Hero hero = entry.hero();
            text.append('\n').append(LINE_TEMPLATE.formatted(
                    hero.rarity().emoji(), hero.name(), hero.rarity().title(), entry.count()));
            rows.add(List.of(new Button(
                    CARD_BUTTON_TEMPLATE.formatted(hero.rarity().emoji(), hero.name(), entry.count()),
                    CallbackPrefix.VIEW_CARD.data(hero.id()))));
        }

        List<Button> navigation = buildNavigation(currentPage, totalPages);
        if (!navigation.isEmpty()) {
            rows.add(navigation);
        }
        return new Message(text.toString(), rows);
    }

    /**
     * Загружает карточки пользователя, группирует их по герою
     * и сортирует: сначала более редкие, затем по имени.
     *
     * @param userId идентификатор пользователя
     * @return отсортированный список записей
     */
    private List<Entry> loadEntries(long userId) {
        Map<Long, Integer> countsByHero = new LinkedHashMap<>();
        for (UserCard card : userCardRepository.findByUserId(userId)) {
            countsByHero.merge(card.heroId(), card.count(), Integer::sum);
        }

        List<Entry> entries = new ArrayList<>();
        countsByHero.forEach((heroId, count) -> {
            if (count > 0) {
                heroRepository.findById(heroId).ifPresent(hero -> entries.add(new Entry(hero, count)));
            }
        });
        entries.sort(Comparator
                .comparing((Entry e) -> e.hero().rarity()).reversed()
                .thenComparing(e -> e.hero().name()));
        return entries;
    }

    /**
     * Строит ряд кнопок пагинации.
     *
     * @param currentPage текущая страница
     * @param totalPages  общее количество страниц
     * @return кнопки навигации (пустой список, если страница одна)
     */
    private List<Button> buildNavigation(int currentPage, int totalPages) {
        List<Button> navigation = new ArrayList<>();
        if (currentPage > 1) {
            navigation.add(new Button(PREVIOUS_BUTTON_TEXT, CallbackPrefix.PAGE.data(currentPage - 1)));
        }
        if (currentPage < totalPages) {
            navigation.add(new Button(NEXT_BUTTON_TEXT, CallbackPrefix.PAGE.data(currentPage + 1)));
        }
        return navigation;
    }

    /**
     * Запись инвентаря: герой и количество копий.
     *
     * @param hero  герой
     * @param count количество копий
     */
    private record Entry(Hero hero, int count) {
    }
}
