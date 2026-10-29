package com.avengers.cardgame.command;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.presentation.HeroRenderer;

public class InventoryRenderer {
    private static final String EMPTY_MESSAGE = "У тебя пока нет карточек. Забери ежедневную награду командой /daily!";
    private static final String HEADER_TEMPLATE = "Твоя коллекция (страница %d/%d):";
    private static final String LINE_TEMPLATE = "%s %s — %s ×%d";
    private static final String CARD_BUTTON_TEMPLATE = "%s %s ×%d";
    private static final String PREVIOUS_BUTTON_TEXT = "◀️ Назад";
    private static final String NEXT_BUTTON_TEXT = "Вперёд ▶️";
    private static final int PAGE_SIZE = 5;
    private final UserCardRepository userCardRepository;
    private final HeroRepository heroRepository;
    private final HeroRenderer heroRenderer;

    public InventoryRenderer(UserCardRepository userCardRepository,
                             HeroRepository heroRepository, HeroRenderer heroRenderer) {
        this.userCardRepository = userCardRepository;
        this.heroRepository = heroRepository;
        this.heroRenderer = heroRenderer;
    }

    public Message render(long userId, int page) {
        List<Entry> entries = loadEntries(userId);
        if (entries.isEmpty()) return new Message(EMPTY_MESSAGE);
        int totalPages = (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int from = (currentPage - 1) * PAGE_SIZE;
        List<Entry> pageEntries = entries.subList(from, Math.min(from + PAGE_SIZE, entries.size()));
        StringBuilder text = new StringBuilder(HEADER_TEMPLATE.formatted(currentPage, totalPages));
        List<List<Button>> rows = new ArrayList<>();
        for (Entry entry : pageEntries) {
            Hero hero = entry.hero();
            HeroRarity rarity = hero.rarity();
            text.append('\n').append(LINE_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), hero.name(), heroRenderer.title(rarity), entry.count()));
            rows.add(List.of(new Button(CARD_BUTTON_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), hero.name(), entry.count()), "/view_card " + hero.id())));
        }
        List<Button> navigation = new ArrayList<>();
        if (currentPage > 1) navigation.add(new Button(PREVIOUS_BUTTON_TEXT, "/inventory_page " + (currentPage - 1)));
        if (currentPage < totalPages) navigation.add(new Button(NEXT_BUTTON_TEXT, "/inventory_page " + (currentPage + 1)));
        if (!navigation.isEmpty()) rows.add(navigation);
        return new Message(text.toString(), rows);
    }

    private List<Entry> loadEntries(long userId) {
        Map<Long, Integer> countsByHero = new LinkedHashMap<>();
        for (UserCard card : userCardRepository.findByUserId(userId)) {
            countsByHero.merge(card.heroId(), card.count(), Integer::sum);
        }
        List<Entry> entries = new ArrayList<>();
        countsByHero.forEach((heroId, count) -> {
            if (count > 0) heroRepository.findById(heroId).ifPresent(hero -> entries.add(new Entry(hero, count)));
        });
        entries.sort(Comparator.comparing((Entry entry) -> entry.hero().rarity()).reversed()
                .thenComparing(entry -> entry.hero().name()));
        return entries;
    }

    private record Entry(Hero hero, int count) {}
}
