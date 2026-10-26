package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 */
public interface UserRepository {

    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByUserId(long userId);

    /**
     * Сохраняет нового пользователя.
     *
     * @param user пользователь для сохранения
     * @throws UserAlreadyExistsException если пользователь с таким идентификатором уже существует
     */
    void save(User user) throws UserAlreadyExistsException;

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user пользователь с обновлёнными данными
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void update(User user) throws UserNotFoundException;

    /**
     * Находит пользователя по идентификатору и блокирует строку до конца
     * транзакции ({@code SELECT ... FOR UPDATE}).
     *
     * @param conn   соединение, привязанное к транзакции
     * @param userId идентификатор пользователя
     * @return пользователь, если найден
     */
    Optional<User> findByUserIdForUpdate(Connection conn, long userId);

    /**
     * Обновляет время последнего получения ежедневной награды в рамках транзакции.
     *
     * @param conn        соединение, привязанное к транзакции
     * @param userId      идентификатор пользователя
     * @param lastDailyAt новое время
     * @throws UserNotFoundException если пользователя нет
     */
    void updateLastDailyAt(Connection conn, long userId, LocalDateTime lastDailyAt)
            throws UserNotFoundException;

    /**
     * Обновляет время последнего получения ежедневной награды.
     *
     * @param userId      идентификатор пользователя
     * @param lastDailyAt новое время получения /daily
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void updateLastDailyAt(long userId, LocalDateTime lastDailyAt) throws UserNotFoundException;

    /**
     * Находит пользователей, которым пора отправить уведомление о доступной
     * ежедневной награде: последняя награда получена не позже указанного
     * момента, и уведомление после неё ещё не отправлялось.
     *
     * @param thresholdTime момент, не позже которого должна быть получена последняя награда
     * @return список пользователей для уведомления
     */
    List<User> findUsersToNotify(LocalDateTime thresholdTime);

    /**
     * Сохраняет время отправки уведомления о готовности ежедневной награды.
     *
     * @param userId     идентификатор пользователя
     * @param notifiedAt время отправки уведомления
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void updateDailyNotifiedAt(long userId, LocalDateTime notifiedAt) throws UserNotFoundException;
}
