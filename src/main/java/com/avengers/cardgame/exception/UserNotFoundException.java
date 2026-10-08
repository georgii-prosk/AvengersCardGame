package com.avengers.cardgame.exception;

/**
 * Исключение, выбрасываемое когда пользователь не найден в базе данных.
 * <p>
 * Наследуется от {@link RuntimeException}, чтобы не заставлять вызывающий код
 * обрабатывать checked-исключение — ситуация «пользователя нет» обычно
 * означает программную ошибку (забыли зарегистрировать), а не ожидаемое
 * событие бизнес-логики.
 */
public class UserNotFoundException extends RuntimeException {

    /** Идентификатор пользователя в Telegram, которого не нашли. */
    private final long telegramId;

    /**
     * Создаёт исключение с указанием Telegram ID ненайденного пользователя.
     *
     * @param telegramId идентификатор пользователя в Telegram
     */
    public UserNotFoundException(long telegramId) {
        super("User not found: telegramId=" + telegramId);
        this.telegramId = telegramId;
    }

    /**
     * Возвращает Telegram ID пользователя, которого не нашли.
     *
     * @return Telegram ID
     */
    public long getTelegramId() {
        return telegramId;
    }
}