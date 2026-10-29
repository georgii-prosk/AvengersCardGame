package com.avengers.cardgame.scheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Обертка над {@link ScheduledExecutorService} для управления периодическими задачами.
 */
public class TaskScheduler {

    /** Исполнитель для запуска и управления задачами. */
    private final ScheduledExecutorService executor;

    /**
     * @param executor исполнитель, который будет запускать задачи
     */
    public TaskScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Запускает задачу с фиксированной задержкой между завершением предыдущего 
     * и началом следующего запуска.
     *
     * @param task     задача для выполнения
     * @param interval интервал между запусками
     * @throws IllegalArgumentException если интервал меньше или равен нулю
     */
    public void scheduleWithFixedDelay(Runnable task, Duration interval) {
        long intervalSeconds = interval.toSeconds();
        if (intervalSeconds <= 0) {
            throw new IllegalArgumentException("Task interval must be positive");
        }

        executor.scheduleWithFixedDelay(
                task,
                0,
                intervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Останавливает исполнитель, инициируя корректное завершение всех запланированных задач.
     */
    public void stop() {
        executor.shutdown();
    }
}