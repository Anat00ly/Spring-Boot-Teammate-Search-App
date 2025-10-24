package com.example.research2.SpringBoot.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Конфигурация кэша для приложения
 * Использует Caffeine как провайдер кэша
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Создает CacheManager с настройками Caffeine
     * - Максимум 1000 записей
     * - Истечение через 10 минут после записи
     * - Истечение через 5 минут после последнего доступа
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("players", "playerById");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()); // Включаем статистику для мониторинга

        return cacheManager;
    }
}