package com.example.research2.SpringBoot.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        logger.info("🔧 Configuring Redis Template...");

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Настройка Jackson для сериализации
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.registerModule(new JavaTimeModule());
        serializer.setObjectMapper(mapper);

        // Устанавливаем сериализаторы
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        logger.info("✅ Redis Template configured successfully!");
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        logger.info("🔧 Configuring Cache Manager...");

        // Настройка Jackson для кэш-менеджера
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.registerModule(new JavaTimeModule());
        serializer.setObjectMapper(mapper);

        // Конфигурация кэша по умолчанию (TTL 1 час)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        // Специфичные конфигурации для разных кэшей
        RedisCacheConfiguration playersConfig = defaultConfig.entryTtl(Duration.ofMinutes(30));
        RedisCacheConfiguration postsConfig = defaultConfig.entryTtl(Duration.ofMinutes(15));
        RedisCacheConfiguration searchConfig = defaultConfig.entryTtl(Duration.ofMinutes(5));
        RedisCacheConfiguration onlineStatusConfig = defaultConfig.entryTtl(Duration.ofMinutes(2));

        logger.info("✅ Cache configurations:");
        logger.info("  - Default TTL: 1 hour");
        logger.info("  - Players TTL: 30 minutes");
        logger.info("  - Posts TTL: 15 minutes");
        logger.info("  - Search TTL: 5 minutes");
        logger.info("  - Online Status TTL: 2 minutes");

        CacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("players", playersConfig)
                .withCacheConfiguration("playerById", playersConfig)
                .withCacheConfiguration("playerByEmail", playersConfig)
                .withCacheConfiguration("posts", postsConfig)
                .withCacheConfiguration("allPosts", postsConfig)
                .withCacheConfiguration("searchPlayers", searchConfig)
                .withCacheConfiguration("searchPosts", searchConfig)
                .withCacheConfiguration("onlineStatus", onlineStatusConfig)
                .withCacheConfiguration("friends", playersConfig)
                .withCacheConfiguration("friendshipStatus", searchConfig)
                .build();

        logger.info("✅ Cache Manager configured successfully!");
        return cacheManager;
    }
}