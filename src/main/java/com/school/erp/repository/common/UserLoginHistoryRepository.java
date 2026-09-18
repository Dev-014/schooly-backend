package com.school.erp.repository;

import com.school.erp.entity.UserLoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UserLoginHistoryRepository extends JpaRepository<UserLoginHistory, Long> {
    Page<UserLoginHistory> findAllByOrderByLoginTimeDesc(Pageable pageable);
    long countByLoginTimeAfter(LocalDateTime date);
}
