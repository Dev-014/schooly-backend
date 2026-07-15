package com.school.erp.repository;

import com.school.erp.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    Optional<AuthSession> findByAccessToken(String accessToken);

    Optional<AuthSession> findByRefreshToken(String refreshToken);

    void deleteByAccessToken(String accessToken);
}
