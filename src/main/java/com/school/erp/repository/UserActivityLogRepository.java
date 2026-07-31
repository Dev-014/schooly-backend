package com.school.erp.repository;

import com.school.erp.entity.UserActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    Page<UserActivityLog> findAllByOrderByTimestampDesc(Pageable pageable);
    Page<UserActivityLog> findBySchoolIdOrderByTimestampDesc(Long schoolId, Pageable pageable);
}
