package com.liligo.reggie.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class ImageService {

    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private RedisTemplate<String, byte[]> byteArrayRedisTemplate;

    // Caffeine 本地缓存
    private final Cache<String, byte[]> localCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 二级缓存获取图片字节数组
     * L1: Caffeine 本地缓存 (2分钟)
     * L2: Redis 分布式缓存 (1小时)
     * L3: 磁盘文件
     */
    public byte[] getImageBytes(String name) throws IOException {
        String redisKey = "reggie_redisImageCache::" + name;

        // L1: 先检查 Caffeine 本地缓存
        byte[] cachedBytes = localCache.getIfPresent(name);
        if (cachedBytes != null) {
            log.info("L1缓存命中 (Caffeine): {}", name);
            return cachedBytes;
        }

        // L2: 检查 Redis 缓存
        try {
            cachedBytes = byteArrayRedisTemplate.opsForValue().get(redisKey);
            if (cachedBytes != null) {
                log.info("L2缓存命中 (Redis): {}", name);
                // 回填到 L1 缓存
                localCache.put(name, cachedBytes);
                return cachedBytes;
            }
        } catch (Exception e) {
            log.warn("Redis缓存读取失败: {}", e.getMessage());
        }

        // L3: 从磁盘读取文件
        log.info("缓存未命中，从磁盘读取文件: {}", name);
        byte[] fileBytes = loadImageFromDisk(name);

        if (fileBytes != null) {
            // 异步存储到两级缓存
            CompletableFuture.runAsync(() -> {
                try {
                    // 存入 Redis (L2)
                    byteArrayRedisTemplate.opsForValue().set(redisKey, fileBytes, Duration.ofHours(1));
                    log.info("文件已存入Redis缓存: {}", name);
                } catch (Exception e) {
                    log.warn("Redis缓存存储失败: {}", e.getMessage());
                }
            });

            // 存入 Caffeine (L1)
            localCache.put(name, fileBytes);
            log.info("文件已存入本地缓存: {}", name);
        }

        return fileBytes;
    }

    /**
     * 从磁盘加载图片
     */
    private byte[] loadImageFromDisk(String name) throws IOException {
        Path fileLocation = Paths.get(basePath).resolve(name).normalize();

        // 安全检查
        if (!fileLocation.startsWith(Paths.get(basePath).normalize())) {
            log.error("非法的文件路径访问尝试: {}", name);
            return null;
        }

        // 文件存在性检查
        if (!Files.exists(fileLocation)) {
            log.warn("请求的文件不存在: {}", fileLocation);
            return null;
        }

        return Files.readAllBytes(fileLocation);
    }

    /**
     * 清理指定图片的所有缓存
     */
    public void evictImageCache(String name) {
        String redisKey = "reggie_redisImageCache::" + name;

        // 清理 L1 缓存
        localCache.invalidate(name);

        // 清理 L2 缓存
        try {
            byteArrayRedisTemplate.delete(redisKey);
            log.info("已清理图片缓存: {}", name);
        } catch (Exception e) {
            log.warn("清理Redis缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 清理所有图片缓存
     */
    public void clearAllImageCache() {
        // 清理 L1 缓存
        localCache.invalidateAll();

        // 清理 L2 缓存
        try {
            Set<String> keys = byteArrayRedisTemplate.keys("reggie_redisImageCache::*");
            if (keys != null && !keys.isEmpty()) {
                byteArrayRedisTemplate.delete(keys);
                log.info("清理了 {} 个Redis缓存项", keys.size());
            }
        } catch (Exception e) {
            log.warn("清理Redis缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        CacheStats stats = localCache.stats();
        return String.format("本地缓存统计 - 命中率: %.2f%%, 命中次数: %d, 未命中次数: %d, 缓存大小: %d",
                stats.hitRate() * 100,
                stats.hitCount(),
                stats.missCount(),
                localCache.estimatedSize());
    }
}