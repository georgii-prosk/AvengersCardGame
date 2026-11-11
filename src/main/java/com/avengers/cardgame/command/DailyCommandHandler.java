package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.presentation.DailyRenderer;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.DailyRewardService;

import java.time.Duration;
import java.util.List;

/**
 * Обработчик команды /daily: выдает ежедневные карточки или сообщает о кулдауне.
 */
public class DailyCommandHandler implements CommandHandler {

    /** Сервис выдачи ежедневной награды. */
    private final DailyRewardService dailyRewardService;

    /** Рендерер ежедневной награды. */
    private final DailyRenderer dailyRenderer = new DailyRenderer();

    /**
     * @param dailyRewardService сервис выдачи ежедневной награды
     */
    public DailyCommandHandler(DailyRewardService dailyRewardService) {
        this.dailyRewardService = dailyRewardService;
    }

    @Override
    public void handle(CommandContext context) throws UserNotFoundException {
        DailyRewardResult result = dailyRewardService.grantDaily(context.userId());

        if (result instanceof DailyRewardResult.Cooldown(Duration timeLeft)) {
            context.bot().sendMessage(dailyRenderer.renderCooldown(timeLeft), context.chatId());
            return;
        }

        if (result instanceof DailyRewardResult.Success(List<Hero> heroes)) {
            context.bot().sendMessage(dailyRenderer.renderCongrats(heroes.size()), context.chatId());
            for (Message card : dailyRenderer.renderCards(heroes)) {
                context.bot().sendMessage(card, context.chatId());
            }
        }
    }
}
