package com.school.erp.repository;

import com.school.erp.entity.ImpersonationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImpersonationSessionRepository extends JpaRepository<ImpersonationSession, Long> {
    Optional<ImpersonationSession> findFirstByOriginalUserIdAndStatusOrderByStartedAtDesc(Long originalUserId, String status);
}
