package com.school.erp.repository;

import com.school.erp.entity.SchoolSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolSubscriptionRepository extends JpaRepository<SchoolSubscription, Long> {
    Optional<SchoolSubscription> findFirstBySchoolIdAndStatusOrderByIdDesc(Long schoolId, String status);
    java.util.List<SchoolSubscription> findAllBySchoolIdAndStatus(Long schoolId, String status);
}
