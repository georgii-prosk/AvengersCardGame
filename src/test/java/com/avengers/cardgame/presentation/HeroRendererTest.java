package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HeroRendererTest {

    private final HeroRenderer renderer = new HeroRenderer();

    @Test
    @DisplayName("Каждая редкость имеет эмодзи")
    void everyRarityHasEmoji() {
        for (HeroRarity rarity : HeroRarity.values()) {
            Assertions.assertFalse(renderer.emoji(rarity).isBlank());
        }
    }

    @Test
    @DisplayName("Каждая редкость имеет название")
    void everyRarityHasTitle() {
        for (HeroRarity rarity : HeroRarity.values()) {
            Assertions.assertFalse(renderer.title(rarity).isBlank());
        }
    }

    @Test
    @DisplayName("Карточка содержит эмодзи, имя, редкость и статы")
    void cardContainsEmojiNameRarityAndStats() {
        Hero hero = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "desc");

        String text = renderer.render(hero);

        Assertions.assertEquals(String.join("\n",
                "🟣 Iron Man",
                "Редкость: Эпическая",
                "⚔️ Атака: 80",
                "🛡️ Защита: 90",
                "❤️ Здоровье: 50",
                "desc") + "\n", text);
    }

    @Test
    @DisplayName("С количеством добавляется строка «В коллекции»")
    void cardWithCountShowsCollectionLine() {
        Hero hero = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "desc");

        String text = renderer.render(hero, 3);

        Assertions.assertEquals(String.join("\n",
                "⚪ Pepper Potts",
                "Редкость: Обычная",
                "⚔️ Атака: 5",
                "🛡️ Защита: 15",
                "❤️ Здоровье: 20",
                "desc",
                "В коллекции: ×3"), text);
    }
}