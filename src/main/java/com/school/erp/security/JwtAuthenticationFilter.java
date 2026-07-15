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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
        String uri = request.getRequestURI();
        String servletPath = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublicPath(uri) || isPublicPath(servletPath)) {
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

            List<SimpleGrantedAuthority> authorities = authenticatedUser.role() != null
                    ? List.of(new SimpleGrantedAuthority("ROLE_" + authenticatedUser.role().name()))
                    : List.of();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (UnauthorizedException | IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), ApiResponse.error("Invalid or expired token"));
        } finally {
            AuthContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private boolean isPublicPath(String requestUri) {
        if (requestUri == null) {
            return false;
        }
        // /auth/me and /auth/logout require authentication — they are NOT public
        if (requestUri.equals("/auth/me") || requestUri.equals("/auth/logout") ||
            requestUri.equals("/api/auth/me") || requestUri.equals("/api/auth/logout") ||
            requestUri.equals("/api/v1/auth/me") || requestUri.equals("/api/v1/auth/logout")) {
            return false;
        }
        return requestUri.equals("/health") ||
               requestUri.startsWith("/auth/") ||
               requestUri.startsWith("/api/auth/") ||
               requestUri.startsWith("/api/v1/auth/") ||
               requestUri.startsWith("/onboarding") ||
               requestUri.startsWith("/api/onboarding") ||
               requestUri.startsWith("/api/v1/onboarding") ||
               requestUri.startsWith("/swagger-ui") ||
               requestUri.equals("/swagger-ui.html") ||
               requestUri.startsWith("/v3/api-docs") ||
               requestUri.startsWith("/webjars") ||
               requestUri.equals("/error") ||
               requestUri.equals("/");
    }
}
