package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;

public class HeroRenderer {
    private static final String CARD_TEMPLATE = """
            %s %s
            Редкость: %s
            ⚔️ Атака: %d
            🛡️ Защита: %d
            ❤️ Здоровье: %d
            """;

    public String render(Hero hero) { return render(hero, null); }

    public String render(Hero hero, Integer count) {
        String text = CARD_TEMPLATE.formatted(hero.rarity().emoji(), hero.name(),
                hero.rarity().title(), hero.attack(), hero.defense(), hero.health());
        if (hero.description() != null && !hero.description().isBlank()) text += hero.description().strip() + "\n";
        if (count != null) text += "В коллекции: ×" + count;
        return text;
    }
}
