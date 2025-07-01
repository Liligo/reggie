package com.liligo.reggie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Configuration
@EnableCaching
public class RedisConfig {

    // 添加缓存管理器
    // 新增多级缓存配置
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // 注册JavaTimeModule以支持Java 8时间类型的序列化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 禁用将日期序列化为时间戳的功能
        // 添加以下配置解决Result类型识别问题
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        // Caffeine本地缓存配置（一级缓存）
        CaffeineCacheManager caffeineManager = new CaffeineCacheManager("localCache");
        caffeineManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES));

        // Redis缓存配置（二级缓存）
        RedisCacheConfiguration redisDefaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(objectMapper)    // 使用自定义的ObjectMapper进行序列化
            ))
            .entryTtl(Duration.ofHours(1));  // 设置缓存过期时间为1小时;

        // 验证码专用配置（5分钟过期）
        RedisCacheConfiguration userCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(objectMapper)))
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues();

        // 构建Redis缓存管理器
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(redisDefaultConfig) // 默认配置
                .withCacheConfiguration("userCache", userCacheConfig) // 特殊配置
                .build();

        // 组合缓存管理器
        return new CompositeCacheManager(
                caffeineManager,
                redisCacheManager
        );
    }
}