package com.part4.team09.otboo.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    // Caffeine 로컬 캐시 + Redis 분산 캐시 관리
    @Bean
    public CacheManager compositeCacheManager(RedisConnectionFactory factory) {
        // RedisCacheManager 설정 > 분산 캐시 > 여러 애플리케이션 서버 공유
        RedisCacheManager redisManager = RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        // 로컬 캐시보다 TTL을 길게 설정하여 계층적 만료 구현
                        .entryTtl(Duration.ofMinutes(10)))
                .build();

        // CaffeineCacheManager 설정
        CaffeineCacheManager localManager = new CaffeineCacheManager("followSummary"); // TODO: 조회 기능 추가 후 캐시 이름 파라미터에 추가 예정
        localManager.setCaffeine(Caffeine.newBuilder()
                // Redis 보다 짧은 TTL로 최신 데이터 보장
                .expireAfterWrite(5, TimeUnit.MINUTES)
                // 최대 5,000개 항목으로 메모리 사용량 제한
                // LRU 정책 사용
                .maximumSize(5000));

        // CompositeCacheManager 조합
        // 순서가 중요 > localManager를 먼저 배치하여 로컬 캐싱에게 우선순위를 줌
        CompositeCacheManager composite = new CompositeCacheManager(localManager, redisManager);
        // fallbackToNoOpCache > 모든 캐시 매니저에서 캐시를 찾지 못한 경우
        // NoOpCache를 반환 -> null 대신 빈 캐시 동작 수행
        // 위 동작으로 캐시 관련 NPE 방지하고 일관된 동작 보장
        composite.setFallbackToNoOpCache(true);
        return composite;
    }


    // 직렬화 로직
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer(redisObjectMapper)
                        )
                )
                .prefixCacheNameWith("discodeit:")
                .entryTtl(Duration.ofSeconds(600))
                .disableCachingNullValues();
    }
}