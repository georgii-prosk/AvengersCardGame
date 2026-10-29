package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;

/**
 * Формирует текстовое представление карточек героев и их редкости для отправки пользователю.
 */
public class HeroRenderer {

    /** Шаблон карточки: эмодзи, имя, редкость, атака, защита, здоровье. */
    private static final String CARD_TEMPLATE = """
            %s %s
            Редкость: %s
            ⚔️ Атака: %d
            🛡️ Защита: %d
            ❤️ Здоровье: %d
            """;

    /**
     * Формирует текстовое описание карточки героя без указания количества копий.
     *
     * @param hero герой
     * @return отформатированный текст карточки
     */
    public String render(Hero hero) {
        return render(hero, null);
    }

    /**
     * Формирует текстовое описание карточки героя.
     * <p>
     * Если у героя есть описание, оно добавляется в конец.
     * Если передано количество копий, добавляется строка «В коллекции: ×N».
     *
     * @param hero  герой
     * @param count количество копий у пользователя (может быть {@code null}, если карточка просто просматривается)
     * @return отформатированный текст карточки
     */
    public String render(Hero hero, Integer count) {
        HeroRarity rarity = hero.rarity();

        String text = CARD_TEMPLATE.formatted(
                emoji(rarity),
                hero.name(),
                title(rarity),
                hero.attack(),
                hero.defense(),
                hero.health()
        );

        if (hero.description() != null && !hero.description().isBlank()) {
            text += hero.description().strip() + "\n";
        }

        if (count != null) {
            text += "В коллекции: ×" + count;
        }

        return text;
    }

    /**
     * Возвращает эмодзи, соответствующий редкости героя.
     *
     * @param rarity редкость
     * @return строковое представление эмодзи
     */
    public String emoji(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "⚪";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟡";
        };
    }

    /**
     * Возвращает текстовое название редкости для отображения пользователю.
     *
     * @param rarity редкость
     * @return название редкости (например, "Обычная", "Легендарная")
     */
    public String title(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "Обычная";
            case RARE -> "Редкая";
            case EPIC -> "Эпическая";
            case LEGENDARY -> "Легендарная";
        };
    }
}