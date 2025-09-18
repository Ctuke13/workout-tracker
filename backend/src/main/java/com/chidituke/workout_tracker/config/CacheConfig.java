package com.chidituke.workout_tracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES));

        // Register all cache names used in your services
        cacheManager.setCacheNames(Arrays.asList(
                "user-workout-calendar",
                "user-workout-stats",
                "user-upcoming-workouts",
                "user-recent-workouts",
                "user-workout-week",
                "user-workout-month",
                "user-workout-streak",
                "user-workout-consistency"
        ));

        return cacheManager;
    }
}