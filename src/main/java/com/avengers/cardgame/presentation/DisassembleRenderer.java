package com.avengers.cardgame.presentation;

import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.DisassembleResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Формирует сообщения разбора карточек на кусочки.
 */
public class DisassembleRenderer {

    /** Значение аргумента «все карточки». */
    public static final String ALL_ARGUMENT = "all";

    /** Заголовок выбора карточки. */
    private static final String HEADER_TEMPLATE = "Выбери карточку для разбора (страница %d/%d):";

    /** Сообщение при отсутствии карточек. */
    private static final String EMPTY_MESSAGE = "У тебя пока нет карточек для разбора.";

    /** Префикс команды выбора карточки. */
    private static final String CARD_COMMAND_PREFIX = "/disassemble_pick ";

    /** Префикс команды переключения страницы. */
    private static final String PAGE_COMMAND_PREFIX = "/disassemble_page ";

    /** Текст кнопки «Все». */
    private static final String ALL_BUTTON_TEXT = "Все";

    /** Текст кнопки подтверждения. */
    private static final String CONFIRM_BUTTON_TEXT = "Подтвердить";

    /** Текст кнопки отмены. */
    private static final String CANCEL_BUTTON_TEXT = "Отмена";

    /** Команда отмены. */
    private static final String CANCEL_COMMAND = "/cancel";

    /** Максимальное количество копий, предлагаемое кнопкой быстрого выбора. */
    private static final int QUICK_SELECTION_LIMIT = 5;

    /** Разделитель между эмодзи кусочков и заголовком. */
    private static final String TITLE_SEPARATOR = " ";

    /** Рендерер страниц инвентаря. */
    private final InventoryRenderer inventoryRenderer = new InventoryRenderer();

    /** Рендерер героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /**
     * Формирует страницу коллекции для разбора.
     *
     * @param items      отсортированный список записей инвентаря
     * @param page       номер страницы
     * @param shardsEmoji эмодзи игровых кусочков
     * @return сообщение со страницей коллекции
     */
    public Message renderSelection(@NotNull List<InventoryItem> items, int page, @NotNull String shardsEmoji) {
        String headerTemplate = shardsEmoji + TITLE_SEPARATOR + HEADER_TEMPLATE;
        return inventoryRenderer.render(items, page, EMPTY_MESSAGE, headerTemplate,
                CARD_COMMAND_PREFIX, PAGE_COMMAND_PREFIX);
    }

    /**
     * Формирует сообщение с вариантами количества для разбора.
     *
     * @param hero  выбранный герой
     * @param count доступное количество копий карточки
     * @return сообщение с вариантами количества
     */
    public Message renderQuantity(@NotNull Hero hero, int count) {
        String text = "%s %s [%s] ×%d%nСколько карточек разобрать?".formatted(
                heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(), count);
        List<List<Button>> rows = new ArrayList<>();
        rows.add(quantityButtons(hero.id(), count));
        rows.add(List.of(new Button(CANCEL_BUTTON_TEXT, CANCEL_COMMAND)));
        return new Message(text, rows);
    }

    /**
     * Формирует сообщение с подтверждением разбора.
     *
     * @param hero        выбранный герой
     * @param available   доступное количество копий карточки
     * @param quantity    количество карточек к разбору
     * @param shardsToGain количество получаемых кусочков
     * @param shardsEmoji эмодзи игровых кусочков
     * @return сообщение с подтверждением разбора
     */
    public Message renderConfirmation(@NotNull Hero hero, int available, int quantity,
                                      int shardsToGain, @NotNull String shardsEmoji) {
        String text = "%s %s [%s] ×%d%nРазобрать карточек: %d?%nПолучишь кусочков: %d %s."
                .formatted(heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(),
                        available, quantity, shardsToGain, shardsEmoji);
        return new Message(text, List.of(
                List.of(new Button(CONFIRM_BUTTON_TEXT, "/disassemble_confirm " + hero.id() + " " + quantity),
                        new Button(CANCEL_BUTTON_TEXT, CANCEL_COMMAND))));
    }

    /**
     * Формирует сообщение с результатом разбора.
     *
     * @param result      результат разбора
     * @param shardsEmoji эмодзи игровых кусочков
     * @return сообщение с результатом разбора
     */
    public Message renderResult(@NotNull DisassembleResult result, @NotNull String shardsEmoji) {
        Hero hero = result.hero();
        String text = """
                %s Разобрано: %s %s [%s] ×%d.
                Получено кусочков: %d.
                Твой баланс кусочков: %d.""".formatted(
                shardsEmoji,
                heroRenderer.emoji(hero.rarity()), hero.name(), hero.rarity().displayName(),
                result.cardsDisassembled(), result.shardsGained(), result.shardsBalance());
        return new Message(text);
    }

    /**
     * Формирует сообщение о нехватке карточек.
     *
     * @param heroName  название карточки
     * @param available доступное количество копий карточки
     * @return сообщение о нехватке карточек
     */
    public Message renderNotEnoughCards(@NotNull String heroName, int available) {
        return new Message("❌ У тебя нет столько карточек %s. В коллекции: ×%d."
                .formatted(heroName, available));
    }

    /**
     * Формирует сообщение о некорректном номере страницы.
     *
     * @return сообщение о некорректном номере страницы
     */
    public Message renderInvalidPage() {
        return new Message("Некорректный номер страницы.");
    }

    /**
     * Формирует сообщение о некорректном идентификаторе карточки.
     *
     * @return сообщение о некорректном идентификаторе карточки
     */
    public Message renderInvalidHeroId() {
        return new Message("Некорректный идентификатор карточки.");
    }

    /**
     * Формирует сообщение об отсутствующем герое.
     *
     * @return сообщение об отсутствующем герое
     */
    public Message renderUnknownHero() {
        return new Message("Такой карточки не существует.");
    }

    /**
     * Формирует сообщение об отсутствии карточки в коллекции.
     *
     * @return сообщение об отсутствии карточки в коллекции
     */
    public Message renderCardNotInCollection() {
        return new Message("Этой карточки нет в твоей коллекции.");
    }

    /**
     * Формирует сообщение о некорректном количестве карточек.
     *
     * @return сообщение о некорректном количестве карточек
     */
    public Message renderInvalidQuantity() {
        return new Message("Некорректное количество карточек.");
    }

    /**
     * Возвращает кнопки выбора количества.
     *
     * @param heroId идентификатор героя
     * @param count  доступное количество копий карточки
     * @return ряд кнопок
     */
    private List<Button> quantityButtons(long heroId, int count) {
        Set<Integer> values = new LinkedHashSet<>();
        values.add(1);
        values.add(Math.min(QUICK_SELECTION_LIMIT, count));
        List<Button> buttons = new ArrayList<>();
        for (int value : values) {
            buttons.add(new Button(String.valueOf(value),
                    "/disassemble_qty " + heroId + " " + value));
        }
        buttons.add(new Button(ALL_BUTTON_TEXT, "/disassemble_qty " + heroId + " " + ALL_ARGUMENT));
        return buttons;
    }
}
