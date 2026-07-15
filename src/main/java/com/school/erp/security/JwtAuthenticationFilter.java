package com.school.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.erp.api.ApiResponse;
import com.school.erp.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (header == null || header.isBlank()) {
                throw new UnauthorizedException("Missing Authorization header");
            }

            if (!header.startsWith("Bearer ")) {
                throw new UnauthorizedException("Invalid Authorization header");
            }

            String token = header.substring(7);
            AuthenticatedUser authenticatedUser = jwtUtil.parseAccessToken(token);
            AuthContextHolder.set(authenticatedUser);
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException | IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.error("Invalid or expired token"));
        } finally {
            AuthContextHolder.clear();
        }
    }

    private boolean isPublicPath(String requestUri) {
        return requestUri.equals("/health") ||
               requestUri.startsWith("/auth/") ||
               requestUri.startsWith("/api/auth/") ||
               requestUri.startsWith("/api/v1/auth/") ||
               requestUri.startsWith("/onboarding/") ||
               requestUri.startsWith("/api/onboarding/") ||
               requestUri.startsWith("/api/v1/onboarding/");
    }
}
