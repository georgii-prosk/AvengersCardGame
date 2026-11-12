package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.CraftResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Формирует сообщения крафта карточек.
 */
public class CraftRenderer {

    /** Шаблон заголовка меню крафта. */
    private static final String HEADER_TEMPLATE =
            "🔨 Выбери редкость для крафта. Твой баланс кусочков: %d %s.";

    /** Шаблон надписи на кнопке редкости. */
    private static final String RARITY_BUTTON_TEMPLATE = "%s %s — %d %s";

    /** Количество кнопок редкости в одном ряду. */
    private static final int BUTTONS_PER_ROW = 2;

    /** Префикс команды крафта выбранной редкости. */
    private static final String CRAFT_COMMAND_PREFIX = "/craft_make ";

    /** Сообщение о неизвестной редкости. */
    private static final String UNKNOWN_RARITY_MESSAGE = "Неизвестная редкость крафта.";

    /** Рендерер героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует меню выбора редкости для крафта.
     *
     * @param shards      баланс кусочков игрока
     * @param shardsEmoji эмодзи игровых кусочков
     * @return сообщение с меню выбора редкости
     */
    public Message renderMenu(int shards, @NotNull String shardsEmoji) {
        String text = HEADER_TEMPLATE.formatted(shards, shardsEmoji);
        List<List<Button>> rows = new ArrayList<>();
        List<Button> currentRow = new ArrayList<>();
        for (HeroRarity rarity : HeroRarity.values()) {
            currentRow.add(new Button(RARITY_BUTTON_TEMPLATE.formatted(
                    heroRenderer.emoji(rarity), rarity.displayName(), rarity.craftCost(), shardsEmoji),
                    CRAFT_COMMAND_PREFIX + rarity.name()));
            if (currentRow.size() == BUTTONS_PER_ROW) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }
        return new Message(text, rows);
    }

    /**
     * Формирует сообщение об успешном крафте.
     *
     * @param result результат крафта
     * @return сообщение об успешном крафте
     */
    public Message renderCraftSuccess(@NotNull CraftResult result) {
        Hero hero = result.hero();
        String text = """
                🔨 Крафт успешен! Потрачено кусочков: %d.

                %s %s [%s]
                ⚔️ ATK: %d | 🛡 DEF: %d | ❤️ HP: %d

                Остаток кусочков: %d.""".formatted(
                result.shardsSpent(),
                heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(),
                hero.attack(), hero.defense(), hero.health(), result.shardsBalance());
        return new Message(text);
    }

    /**
     * Формирует сообщение о нехватке кусочков.
     *
     * @param required требуемое количество кусочков
     * @param available доступное количество кусочков
     * @param missing  недостающее количество кусочков
     * @return сообщение о нехватке кусочков
     */
    public Message renderInsufficientShards(int required, int available, int missing) {
        return new Message("❌ Недостаточно кусочков. Нужно: %d, у тебя: %d. Не хватает: %d."
                .formatted(required, available, missing));
    }

    /**
     * Формирует сообщение о неизвестной редкости.
     *
     * @return сообщение о неизвестной редкости
     */
    public Message renderUnknownRarity() {
        return new Message(UNKNOWN_RARITY_MESSAGE);
    }
}
