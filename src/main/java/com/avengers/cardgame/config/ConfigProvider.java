package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.ConfigurationException;

/**
 * Источник конфигурации приложения.
 */
public interface ConfigProvider {

    /**
     * Возвращает конфигурацию приложения.
     *
     * @return конфигурация
     * @throws ConfigurationException если конфигурацию не удалось получить
     */
    AppConfig read() throws ConfigurationException;
}