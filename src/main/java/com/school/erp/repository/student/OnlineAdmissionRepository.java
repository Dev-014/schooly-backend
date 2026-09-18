package com.school.erp.repository;

import com.school.erp.entity.OnlineAdmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OnlineAdmissionRepository extends JpaRepository<OnlineAdmission, Long> {
    List<OnlineAdmission> findBySchoolId(Long schoolId);
    Optional<OnlineAdmission> findByIdAndSchoolId(Long id, Long schoolId);
}
