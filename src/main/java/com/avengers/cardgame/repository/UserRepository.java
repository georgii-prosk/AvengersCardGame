package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Находит пользователя по идентификатору в рамках заданного соединения.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @return пользователь, если найден
     */
    @NotNull Optional<User> findByUserId(@Nullable Connection connection, long userId);

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
     * Списывает валюту с баланса пользователя.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param amount     сумма списания
     * @return {@code true}, если списание выполнено; {@code false}, если средств недостаточно
     */
    boolean spendBalance(@Nullable Connection connection, long userId, int amount);

    /**
     * Списывает кусочки с баланса пользователя.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков
     * @return {@code true}, если списание выполнено; {@code false}, если кусочков недостаточно
     */
    boolean spendShards(@Nullable Connection connection, long userId, int amount);

    /**
     * Начисляет валюту на баланс пользователя.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param amount     сумма начисления
     * @throws UserNotFoundException если пользователь не найден
     */
    void addBalance(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException;

    /**
     * Начисляет кусочки на баланс пользователя.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков
     * @throws UserNotFoundException если пользователь не найден
     */
    void addShards(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException;
}
