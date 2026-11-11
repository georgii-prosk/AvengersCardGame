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

        Assertions.assertTrue(text.contains("🟣"));
        Assertions.assertTrue(text.contains("Iron Man"));
        Assertions.assertTrue(text.contains("Эпическая"));
        Assertions.assertTrue(text.contains("80"));
        Assertions.assertTrue(text.contains("90"));
        Assertions.assertTrue(text.contains("50"));
        Assertions.assertFalse(text.contains("В коллекции"));
    }

    @Test
    @DisplayName("С количеством добавляется строка «В коллекции»")
    void cardWithCountShowsCollectionLine() {
        Hero hero = new Hero(1, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "desc");

        String text = renderer.render(hero, 3);

        Assertions.assertTrue(text.contains("В коллекции: ×3"));
        Assertions.assertTrue(text.contains("desc"));
    }
}