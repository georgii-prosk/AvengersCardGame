package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.MarketItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Формирует текстовое представление лотов рынка с пагинацией.
 * <p>
 * Рендерер не хранит зависимости и не ходит в БД: он принимает готовые объекты данных
 * и возвращает {@link Message}.
 */
public class MarketRenderer {

    /** Сообщение, отображаемое при отсутствии лотов на рынке. */
    private static final String EMPTY_MARKET_MESSAGE = "На рынке пока нет лотов. Стань первым продавцом: /sell";

    /** Сообщение, отображаемое при отсутствии собственных лотов. */
    private static final String EMPTY_OWN_MESSAGE = "У тебя нет активных лотов. Выставь карточку командой /sell.";

    /** Заголовок страницы рынка. */
    private static final String MARKET_HEADER_TEMPLATE = "🏪 Рынок (страница %d/%d):";

    /** Заголовок страницы собственных лотов. */
    private static final String OWN_HEADER_TEMPLATE = "📦 Твои лоты (страница %d/%d):";

    /** Шаблон строки лота (эмодзи, имя, редкость, цена, эмодзи валюты, продавец). */
    private static final String LINE_TEMPLATE = "%s %s — %s | %d %s | Продавец: %s";

    /** Шаблон надписи кнопки покупки. */
    private static final String BUY_BUTTON_TEMPLATE = "Купить за %d %s";

    /** Надпись кнопки снятия собственного лота. */
    private static final String CANCEL_BUTTON_TEXT = "Снять лот";

    /** Надпись кнопки перехода на предыдущую страницу. */
    private static final String PREVIOUS_BUTTON_TEXT = "◀️ Назад";

    /** Надпись кнопки перехода на следующую страницу. */
    private static final String NEXT_BUTTON_TEXT = "Вперёд ▶️";

    /** Команда перехода между страницами рынка. */
    private static final String MARKET_PAGE_COMMAND = "/market_page";

    /** Команда перехода между страницами собственных лотов. */
    private static final String OWN_PAGE_COMMAND = "/market_mine_page";

    /** Количество лотов, отображаемых на одной странице. */
    private static final int PAGE_SIZE = 5;

    /** Рендерер для карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует страницу рынка с кнопками покупки.
     *
     * @param items         активные лоты
     * @param page          запрошенная страница (начиная с 1)
     * @param currencyEmoji эмодзи основной валюты
     * @return сообщение с текстом и кнопками
     */
    public Message render(@NotNull List<MarketItem> items, int page, @NotNull String currencyEmoji) {
        Objects.requireNonNull(currencyEmoji, "Currency emoji must not be null");
        return renderPage(items, page, currencyEmoji, false);
    }

    /**
     * Формирует страницу собственных лотов с кнопками снятия.
     *
     * @param items         лоты продавца
     * @param page          запрошенная страница (начиная с 1)
     * @param currencyEmoji эмодзи основной валюты
     * @return сообщение с текстом и кнопками
     */
    public Message renderOwn(@NotNull List<MarketItem> items, int page, @NotNull String currencyEmoji) {
        Objects.requireNonNull(currencyEmoji, "Currency emoji must not be null");
        return renderPage(items, page, currencyEmoji, true);
    }

    /**
     * Формирует страницу лотов.
     *
     * @param items         лоты
     * @param page          запрошенная страница
     * @param currencyEmoji эмодзи валюты
     * @param own           {@code true} для страницы собственных лотов
     * @return сообщение
     */
    private Message renderPage(List<MarketItem> items, int page, String currencyEmoji, boolean own) {
        Objects.requireNonNull(items, "Items must not be null");
        if (items.isEmpty()) {
            return new Message(own ? EMPTY_OWN_MESSAGE : EMPTY_MARKET_MESSAGE);
        }

        int totalPages = (items.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        int currentPage = Math.clamp(page, 1, totalPages);
        int from = (currentPage - 1) * PAGE_SIZE;
        List<MarketItem> pageItems = items.subList(from, Math.min(from + PAGE_SIZE, items.size()));

        String headerTemplate = own ? OWN_HEADER_TEMPLATE : MARKET_HEADER_TEMPLATE;
        StringBuilder text = new StringBuilder(headerTemplate.formatted(currentPage, totalPages));
        List<List<Button>> rows = new ArrayList<>();

        for (MarketItem item : pageItems) {
            var hero = item.hero();
            var rarity = hero.rarity();
            text.append('\n').append(LINE_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), hero.name(), heroRenderer.title(rarity),
                    item.listing().price(), currencyEmoji, item.sellerName()));

            Button button = own
                    ? new Button(CANCEL_BUTTON_TEXT, "/market_cancel " + item.listing().id())
                    : new Button(BUY_BUTTON_TEMPLATE.formatted(item.listing().price(), currencyEmoji),
                    "/market_buy " + item.listing().id());
            rows.add(List.of(button));
        }

        List<Button> navigation = new ArrayList<>();
        String pageCommand = own ? OWN_PAGE_COMMAND : MARKET_PAGE_COMMAND;
        if (currentPage > 1) {
            navigation.add(new Button(PREVIOUS_BUTTON_TEXT, pageCommand + " " + (currentPage - 1)));
        }
        if (currentPage < totalPages) {
            navigation.add(new Button(NEXT_BUTTON_TEXT, pageCommand + " " + (currentPage + 1)));
        }
        if (!navigation.isEmpty()) rows.add(navigation);

        return new Message(text.toString(), rows);
    }
}
