package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Обработчик крафта карточек из кусочков.
 * <p>
 * Команда состоит из шагов: /craft (меню редкостей) и /craft_make (создание карточки).
 */
public class CraftCommandHandler implements CommandHandler {

    /** Эмодзи кусочков. */
    private static final String SHARDS_EMOJI = "🧩";

    /** Текст меню крафта. */
    private static final String MENU_HEADER = """
            🔨 Крафт героя
            Выбери редкость:
            """;

    /** Текст ответа при некорректной редкости. */
    private static final String INVALID_RARITY_MESSAGE = "Некорректная редкость карточки.";

    /** Сервис карточек. */
    private final CardService cardService;

    /** Рендерер карточек героев. */
    private final HeroRenderer heroRenderer = new HeroRenderer();

    /** Шаг обработки команды. */
    private final Step step;

    /**
     * Шаги команды крафта.
     */
    public enum Step {
        /** Показ меню редкостей. */
        MENU,
        /** Создание карточки выбранной редкости. */
        MAKE
    }

    /**
     * @param cardService сервис карточек
     * @param step        шаг обработки
     */
    public CraftCommandHandler(CardService cardService, @NotNull Step step) {
        this.cardService = cardService;
        this.step = step;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        if (step == Step.MENU) {
            showMenu(context);
            return;
        }
        handleCraft(context);
    }

    /**
     * Показывает меню редкостей с ценами крафта.
     *
     * @param context контекст команды
     */
    private void showMenu(CommandContext context) {
        StringBuilder text = new StringBuilder(MENU_HEADER);
        List<List<Button>> rows = new ArrayList<>();

        for (HeroRarity rarity : HeroRarity.values()) {
            text.append('\n').append("%s %s — %d %s".formatted(
                    heroRenderer.emoji(rarity), heroRenderer.title(rarity),
                    rarity.craftCost(), SHARDS_EMOJI));
            rows.add(List.of(new Button(
                    "%s %s — %d %s".formatted(heroRenderer.emoji(rarity), heroRenderer.title(rarity),
                            rarity.craftCost(), SHARDS_EMOJI),
                    "/craft_make " + rarity.name())));
        }
        rows.add(List.of(new Button("❌ Отмена", "/cancel")));

        context.bot().sendMessage(new Message(text.toString(), rows), context.chatId());
    }

    /**
     * Создаёт карточку выбранной редкости.
     *
     * @param context контекст команды
     */
    private void handleCraft(CommandContext context) throws UserNotFoundException {
        HeroRarity rarity = parseRarity(context.argument());
        if (rarity == null) {
            context.bot().sendMessage(new Message(INVALID_RARITY_MESSAGE), context.chatId());
            return;
        }

        try {
            Hero hero = cardService.craft(context.userId(), rarity);
            context.bot().sendMessage(new Message("✨ Крафт успешен! Ты получил карточку:"), context.chatId());
            context.bot().sendMessage(new Message(heroRenderer.render(hero)), context.chatId());
        } catch (InsufficientShardsException exception) {
            context.bot().sendMessage(new Message("Недостаточно кусочков: нужно %d %s, у тебя %d %s."
                    .formatted(exception.required(), SHARDS_EMOJI,
                            exception.available(), SHARDS_EMOJI)), context.chatId());
        }
    }

    /**
     * Разбирает название редкости из аргумента команды.
     *
     * @param argument текст аргумента
     * @return редкость или {@code null}, если значение неизвестно
     */
    private HeroRarity parseRarity(String argument) {
        if (argument == null || argument.isBlank()) return null;
        try {
            return HeroRarity.valueOf(argument.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
