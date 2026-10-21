package com.avengers.cardgame.command.callback;

/**
 * Префиксы данных inline-кнопок. По префиксу сервис обработки
 * нажатий выбирает нужный {@link CallbackHandler}.
 */
public enum CallbackPrefix {
    /** Просмотр карточки героя: {@code view_card:<heroId>}. */
    VIEW_CARD("view_card"),
    /** Переход на страницу инвентаря: {@code page:<номер>}. */
    PAGE("page");

    /** Разделитель префикса и аргумента. */
    private static final String SEPARATOR = ":";

    /** Строковое значение префикса. */
    private final String value;

    /**
     * @param value строковое значение префикса
     */
    CallbackPrefix(String value) {
        this.value = value;
    }

    /**
     * @return строковое значение префикса
     */
    public String value() {
        return value;
    }

    /**
     * Собирает данные кнопки из префикса и аргумента.
     *
     * @param argument аргумент кнопки
     * @return строка вида {@code prefix:argument}
     */
    public String data(Object argument) {
        return value + SEPARATOR + argument;
    }
}
