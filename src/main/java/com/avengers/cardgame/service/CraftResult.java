package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import org.jetbrains.annotations.NotNull;

/**
 * Результат крафта карточки.
 *
 * @param hero         полученный герой
 * @param shardsSpent  потраченное количество кусочков
 * @param shardsBalance баланс кусочков после операции
 */
public record CraftResult(
        @NotNull Hero hero,
        int shardsSpent,
        int shardsBalance
) {
}
