package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.User;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 * <p>
 * Определяет контракт доступа к данным пользователей. Реализации могут
 * использовать различные источники хранения (БД, in-memory и т.п.).
 *
 * @see com.avengers.cardgame.repository.impl.UserRepositoryImpl
 */
public interface UserRepository {

    /**
     * Находит пользователя по его идентификатору в Telegram.
     *
     * @param telegramId идентификатор пользователя в Telegram
     * @return {@link Optional} с пользователем, если он найден, иначе пустой {@link Optional}
     */
    Optional<User> findByTelegramId(long telegramId);

    /**
     * Сохраняет нового пользователя.
     *
     * @param user пользователь для сохранения
     */
    void save(User user);

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user пользователь с обновлёнными данными
     */
    void update(User user);
}