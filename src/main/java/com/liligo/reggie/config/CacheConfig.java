package com.liligo.reggie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary // 声明这是应用程序的主要缓存管理器
    public CacheManager cacheManager(RedisConnectionFactory factory){
        // 1. 创建一个通用的、基于JSON的Redis缓存配置，作为默认配置
        RedisCacheConfiguration defaultConfig = createJsonCacheConfig(Duration.ofHours(1));

        // 2. 创建 Redis 缓存管理器，为特定的缓存名称提供独立的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        // 为 userCache 设置5分钟过期
        cacheConfigurations.put("userCache", createJsonCacheConfig(Duration.ofMinutes(5)));

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
//                .disableCreateOnMissingCache()
                .transactionAware()
                .build();

        return redisCacheManager;
    }

    // 创建一个通用的、使用 JSON 序列化的 Redis 配置
    private RedisCacheConfiguration createJsonCacheConfig(Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(buildObjectMapper())))
                .entryTtl(ttl)
                .disableCachingNullValues();
    }

    // 构建通用的 ObjectMapper
    private ObjectMapper buildObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // 注册 JavaTimeModule 以支持 Java 8 时间类型的序列化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);   // 禁用将日期序列化为时间戳的功能
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        return objectMapper;
    }

    // 专门用于图片缓存的 RedisTemplate
    @Bean
    public RedisTemplate<String, byte[]> byteArrayRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 序列化器 - 直接存储字节数组
        template.setValueSerializer(RedisSerializer.byteArray());
        template.setHashValueSerializer(RedisSerializer.byteArray());

        template.afterPropertiesSet();
        return template;
    }
}