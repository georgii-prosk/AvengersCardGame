package com.avengers.cardgame.config;

import com.avengers.cardgame.exception.AppException;

/**
 * Источник конфигурации приложения.
 */
public interface ConfigProvider {

    /**
     * Возвращает конфигурацию приложения.
     *
     * @return конфигурация
     * @throws AppException если конфигурацию не удалось получить
     */
    AppConfig read() throws AppException;
}