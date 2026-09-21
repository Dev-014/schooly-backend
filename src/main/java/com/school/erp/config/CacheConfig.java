package com.school.erp.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_SCHOOL_STATUS = "schoolStatus";
    public static final String CACHE_ENTITLEMENTS = "entitlements";
    public static final String CACHE_USER_PERMISSIONS = "userPermissions";
    public static final String CACHE_USER_SCHOOLS = "userSchools";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(List.of(
                CACHE_SCHOOL_STATUS,
                CACHE_ENTITLEMENTS,
                CACHE_USER_PERMISSIONS,
                CACHE_USER_SCHOOLS
        ));
        return cacheManager;
    }
}
