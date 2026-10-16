package com.avengers.cardgame.config;

/**
 * Источник конфигурации приложения.
 */
public interface ConfigProvider {

    /**
     * Возвращает конфигурацию приложения.
     *
     * @return конфигурация
     */
    AppConfig read();
}