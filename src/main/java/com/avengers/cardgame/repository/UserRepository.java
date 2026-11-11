package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import org.jetbrains.annotations.NotNull;

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
    @NotNull Optional<User> findByUserId(long userId);

    /**
     * Сохраняет нового пользователя.
     *
     * @param user пользователь для сохранения
     * @throws UserAlreadyExistsException если пользователь с таким идентификатором уже существует
     */
    void save(@NotNull User user) throws UserAlreadyExistsException;

    /**
     * Обновляет данные существующего пользователя.
     *
     * @param user пользователь с обновлёнными данными
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void update(@NotNull User user) throws UserNotFoundException;

    /**
     * Обновляет время последнего получения ежедневной награды.
     *
     * @param userId      идентификатор пользователя
     * @param lastDailyAt новое время получения /daily
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void updateLastDailyAt(long userId, @NotNull LocalDateTime lastDailyAt) throws UserNotFoundException;

    /**
     * Находит пользователей, которым пора отправить уведомление о доступной
     * ежедневной награде: последняя награда получена не позже указанного
     * момента, и уведомление после неё ещё не отправлялось.
     *
     * @param thresholdTime момент, не позже которого должна быть получена последняя награда
     * @return список пользователей для уведомления
     */
    @NotNull List<User> findUsersToNotify(@NotNull LocalDateTime thresholdTime);

    /**
     * Сохраняет время отправки уведомления о готовности ежедневной награды.
     *
     * @param userId     идентификатор пользователя
     * @param notifiedAt время отправки уведомления
     * @throws UserNotFoundException если пользователя с таким идентификатором нет
     */
    void updateDailyNotifiedAt(long userId, @NotNull LocalDateTime notifiedAt) throws UserNotFoundException;

    /**
     * Атомарно списывает валюту с баланса пользователя в рамках переданного соединения.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param amount     сумма списания (неотрицательная)
     * @return {@code true}, если списание выполнено; {@code false}, если средств недостаточно
     *         или пользователь не найден
     */
    boolean spendBalance(@NotNull Connection connection, long userId, int amount);

    /**
     * Атомарно списывает кусочки у пользователя в рамках переданного соединения.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков (неотрицательное)
     * @return {@code true}, если списание выполнено; {@code false}, если кусочков недостаточно
     *         или пользователь не найден
     */
    boolean spendShards(@NotNull Connection connection, long userId, int amount);

    /**
     * Атомарно начисляет валюту на баланс пользователя в рамках переданного соединения.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param amount     сумма начисления (неотрицательная)
     * @return {@code true}, если начисление выполнено; {@code false}, если пользователь не найден
     */
    boolean addBalance(@NotNull Connection connection, long userId, int amount);

    /**
     * Атомарно начисляет кусочки пользователю в рамках переданного соединения.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков (неотрицательное)
     * @return {@code true}, если начисление выполнено; {@code false}, если пользователь не найден
     */
    boolean addShards(@NotNull Connection connection, long userId, int amount);
}
