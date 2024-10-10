package com.michael.spring_boot_security.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;


@Slf4j
public class CacheStore<K, V> {
    private final Cache<K, V> cache;

    // Конструктор для инициализации кэша с заданным временем жизни
    public CacheStore(int expiryDuration, TimeUnit timeUnit) {
        // Создание кэша с заданной конфигурацией
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expiryDuration, timeUnit) // Установка времени жизни записи
                .concurrencyLevel(Runtime.getRuntime().availableProcessors()) // Установка уровня конкурентности
                .build(); // Построение кэша
    }

    // Метод для получения значения по ключу из кэша
    public V get(@NotNull K key) {
        log.info("Retrieving from Cache with key {}", key.toString());
        return cache.getIfPresent(key); // Возвращение значения, если оно присутствует в кэше
    }

    // Метод для добавления записи в кэш
    public void put(@NotNull K key, @NotNull V value) {
        log.info("Storing record in Cache for key {}", key.toString());
        cache.put(key, value); // Сохранение записи в кэше
    }

    // Метод для удаления записи из кэша по ключу
    public void evict(@NotNull K key) {
        log.info("Removing from Cache with key {}", key.toString());
        cache.invalidate(key); // Удаление записи из кэша
    }
}
