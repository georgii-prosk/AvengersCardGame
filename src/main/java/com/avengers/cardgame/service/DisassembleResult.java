package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import org.jetbrains.annotations.NotNull;

/**
 * Результат разбора карточек на кусочки.
 *
 * @param hero             разобранный герой
 * @param cardsDisassembled количество разобранных карточек
 * @param shardsGained     полученное количество кусочков
 * @param shardsBalance    баланс кусочков после операции
 */
public record DisassembleResult(
        @NotNull Hero hero,
        int cardsDisassembled,
        int shardsGained,
        int shardsBalance
) {
}
