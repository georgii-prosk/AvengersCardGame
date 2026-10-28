package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;

/** Формирует текстовое представление героев и их редкости. */
public class HeroRenderer {
    private static final String CARD_TEMPLATE = """
            %s %s
            Редкость: %s
            ⚔️ Атака: %d
            🛡️ Защита: %d
            ❤️ Здоровье: %d
            """;

    public String render(Hero hero) {
        return render(hero, null);
    }

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

    public String emoji(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "⚪";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟡";
        };
    }

    public String title(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "Обычная";
            case RARE -> "Редкая";
            case EPIC -> "Эпическая";
            case LEGENDARY -> "Легендарная";
        };
    }
}