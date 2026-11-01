package com.avengers.cardgame.presentation;

import com.avengers.cardgame.model.Hero;

/**
 * Запись инвентаря: герой и количество его копий.
 *
 * @param hero  герой
 * @param count количество копий
 */
public record InventoryItem(Hero hero, int count) {
}