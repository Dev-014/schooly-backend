package com.school.erp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (e.g., auth, health, onboarding, swagger)
                .requestMatchers("/auth/**", "/onboarding/**", "/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Super admin endpoints – require SUPER_ADMIN role
                .requestMatchers("/super-admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
