package com.school.erp.repository;

import com.school.erp.entity.StudentReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentReferralRepository extends JpaRepository<StudentReferral, Long> {
    List<StudentReferral> findBySchoolId(Long schoolId);
    Optional<StudentReferral> findByIdAndSchoolId(Long id, Long schoolId);
}
