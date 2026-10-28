
package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;

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
                rarityEmoji(rarity),
                hero.name(),
                rarityTitle(rarity),
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

    private String rarityEmoji(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "⚪";
            case RARE -> "🔵";
            case EPIC -> "🟣";
            case LEGENDARY -> "🟡";
        };
    }

    private String rarityTitle(HeroRarity rarity) {
        return switch (rarity) {
            case COMMON -> "Обычная";
            case RARE -> "Редкая";
            case EPIC -> "Эпическая";
            case LEGENDARY -> "Легендарная";
        };
    }
}
