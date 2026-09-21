package com.school.erp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lightweight performance logging filter to measure request latency across all endpoints.
 * Flags slow requests (> 500ms) with a warning log for fast diagnosis in production.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class ApiPerformanceFilter extends OncePerRequestFilter {

    private static final long SLOW_THRESHOLD_MS = 500L;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Skip noise from static asset paths if any
            if (!uri.startsWith("/swagger-ui") && !uri.startsWith("/v3/api-docs")) {
                if (duration > SLOW_THRESHOLD_MS) {
                    log.warn("[PERF-SLOW] {} {} completed in {}ms with status {}", method, uri, duration, status);
                } else {
                    log.info("[PERF] {} {} completed in {}ms with status {}", method, uri, duration, status);
                }
            }
        }
    }
}
