
package com.avengers.cardgame.scheduler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Общий механизм запуска периодических задач.
 */
public class TaskScheduler {
    private final ScheduledExecutorService executor;

    public TaskScheduler(ScheduledExecutorService executor) {
        this.executor = executor;
    }

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

    public void stop() {
        executor.shutdown();
    }
}
