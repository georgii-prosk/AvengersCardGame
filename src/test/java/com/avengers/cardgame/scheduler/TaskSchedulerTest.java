package com.avengers.cardgame.scheduler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Тесты для {@link TaskScheduler}. */
class TaskSchedulerTest {

    @Test
    @DisplayName("Запускает переданную задачу")
    void runsScheduledTask() throws InterruptedException {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler scheduler = new TaskScheduler(executor);
        CountDownLatch executed = new CountDownLatch(1);

        scheduler.scheduleWithFixedDelay(executed::countDown, Duration.ofSeconds(1));

        Assertions.assertTrue(executed.await(5, TimeUnit.SECONDS));
        scheduler.stop();
    }

    @Test
    @DisplayName("Отклоняет нулевой интервал")
    void rejectsNonPositiveInterval() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler scheduler = new TaskScheduler(executor);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleWithFixedDelay(() -> { }, Duration.ZERO));
        scheduler.stop();
    }

    @Test
    @DisplayName("stop останавливает исполнитель")
    void stopShutsDownExecutor() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        new TaskScheduler(executor).stop();

        Assertions.assertTrue(executor.isShutdown());
    }
}