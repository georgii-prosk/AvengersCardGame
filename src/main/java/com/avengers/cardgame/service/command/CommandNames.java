package com.avengers.cardgame.service.command;

/**
 * Имена команд бота.
 */
public final class CommandNames {

    /** Команда запуска бота. */
    public static final String START = "/start";

    /** Команда справки. */
    public static final String HELP = "/help";

    /** Команда профиля. */
    public static final String PROFILE = "/profile";

    /** Команда изменения имени. */
    public static final String SET_NAME = "/setname";

    /** Команда изменения описания. */
    public static final String SET_BIO = "/setbio";

    /** Команда ежедневной награды. */
    public static final String DAILY = "/daily";

    /** Команда инвентаря. */
    public static final String INVENTORY = "/inventory";

    /** Команда переключения страницы инвентаря. */
    public static final String INVENTORY_PAGE = "/inventory_page";

    /** Команда просмотра карточки. */
    public static final String VIEW_CARD = "/view_card";

    /** Команда разбора карточек. */
    public static final String DISASSEMBLE = "/disassemble";

    /** Команда переключения страницы разбора. */
    public static final String DISASSEMBLE_PAGE = "/disassemble_page";

    /** Команда выбора карточки для разбора. */
    public static final String DISASSEMBLE_PICK = "/disassemble_pick";

    /** Команда выбора количества карточек для разбора. */
    public static final String DISASSEMBLE_QTY = "/disassemble_qty";

    /** Команда подтверждения разбора. */
    public static final String DISASSEMBLE_CONFIRM = "/disassemble_confirm";

    /** Команда крафта. */
    public static final String CRAFT = "/craft";

    /** Команда крафта выбранной редкости. */
    public static final String CRAFT_MAKE = "/craft_make";

    /** Команда выставления лота на продажу. */
    public static final String SELL = "/sell";

    /** Команда переключения страницы продажи. */
    public static final String SELL_PAGE = "/sell_page";

    /** Команда выбора карточки для продажи. */
    public static final String SELL_PICK = "/sell_pick";

    /** Команда выбора цены лота. */
    public static final String SELL_PRICE = "/sell_price";

    /** Команда подтверждения лота. */
    public static final String SELL_CONFIRM = "/sell_confirm";

    /** Команда рынка. */
    public static final String MARKET = "/market";

    /** Команда переключения страницы рынка. */
    public static final String MARKET_PAGE = "/market_page";

    /** Команда фильтра рынка. */
    public static final String MARKET_FILTER = "/market_filter";

    /** Команда покупки лота. */
    public static final String MARKET_BUY = "/market_buy";

    /** Команда подтверждения покупки лота. */
    public static final String MARKET_BUY_CONFIRM = "/market_buy_confirm";

    /** Команда собственных лотов. */
    public static final String MARKET_MINE = "/market_mine";

    /** Команда снятия лота. */
    public static final String MARKET_CANCEL = "/market_cancel";

    /** Команда отмены действия. */
    public static final String CANCEL = "/cancel";

    private CommandNames() {
    }
}
