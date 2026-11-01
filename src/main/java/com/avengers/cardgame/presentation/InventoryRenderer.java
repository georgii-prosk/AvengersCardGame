package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Формирует текстовое представление инвентаря пользователя с пагинацией.
 */
public class InventoryRenderer {
    /** Сообщение, отображаемое при отсутствии карточек. */
    private static final String EMPTY_MESSAGE = "У тебя пока нет карточек. Забери ежедневную награду командой /daily!";

    /** Шаблон заголовка страницы (текущая страница / всего страниц). */
    private static final String HEADER_TEMPLATE = "Твоя коллекция (страница %d/%d):";

    /** Шаблон строки карточки в тексте (эмодзи, имя, редкость, количество). */
    private static final String LINE_TEMPLATE = "%s %s — %s ×%d";

    /** Шаблон надписи на кнопке карточки (эмодзи, имя, количество). */
    private static final String CARD_BUTTON_TEMPLATE = "%s %s ×%d";

    /** Надпись кнопки перехода на предыдущую страницу. */
    private static final String PREVIOUS_BUTTON_TEXT = "◀️ Назад";

    /** Надпись кнопки перехода на следующую страницу. */
    private static final String NEXT_BUTTON_TEXT = "Вперёд ▶️";

    /** Количество карточек, отображаемых на одной странице. */
    private static final int PAGE_SIZE = 5;

    /** Рендерер для карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует сообщение со страницей инвентаря.
     * Если карточек нет — возвращает сообщение об этом.
     * Если номер страницы вне диапазона — приводит к ближайшей существующей.
     *
     * @param items отсортированный список записей (герой и количество его копий)
     * @param page  запрошенная страница (начиная с 1)
     * @return сообщение с текстом и кнопками навигации
     */
    public Message render(List<InventoryItem> items, int page) {
        if (items.isEmpty()) {
            return new Message(EMPTY_MESSAGE);
        }

        int totalPages = (items.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = Math.clamp(page, 1, totalPages);

        int from = (currentPage - 1) * PAGE_SIZE;
        List<InventoryItem> pageItems = items.subList(from, Math.min(from + PAGE_SIZE, items.size()));

        StringBuilder text = new StringBuilder(HEADER_TEMPLATE.formatted(currentPage, totalPages));
        List<List<Button>> rows = new ArrayList<>();

        for (InventoryItem item : pageItems) {
            var hero = item.hero();
            var rarity = hero.rarity();
            text.append('\n').append(LINE_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), hero.name(), heroRenderer.title(rarity), item.count()));
            rows.add(List.of(new Button(CARD_BUTTON_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), hero.name(), item.count()), "/view_card " + hero.id())));
        }

        List<Button> navigation = new ArrayList<>();
        if (currentPage > 1) navigation.add(new Button(PREVIOUS_BUTTON_TEXT, "/inventory_page " + (currentPage - 1)));
        if (currentPage < totalPages) navigation.add(new Button(NEXT_BUTTON_TEXT, "/inventory_page " + (currentPage + 1)));
        if (!navigation.isEmpty()) rows.add(navigation);

        return new Message(text.toString(), rows);
    }
}