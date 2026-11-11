package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.UserNotFoundException;

/**
 * Сервис для работы с карточками пользователей.
 */
public interface CardService {

    /**
     * Выдаёт ежедневную награду пользователю.
     * <p>
     * Если с прошлой выдачи прошло достаточно времени, генерирует карточки
     * и обновляет время последней выдачи. В противном случае возвращает
     * информацию о том, сколько осталось ждать.
     *
     * @param userId идентификатор пользователя
     * @return результат выдачи (список героев или оставшееся время кулдауна)
     * @throws UserNotFoundException если пользователь с таким идентификатором не найден
     */
    DailyRewardResult grantDaily(long userId) throws UserNotFoundException;
}