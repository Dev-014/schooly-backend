package com.school.erp.repository.student;

import com.school.erp.entity.student.StudentLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentLeaveRepository extends JpaRepository<StudentLeave, Long> {
    List<StudentLeave> findBySchoolId(Long schoolId);
    Optional<StudentLeave> findByIdAndSchoolId(Long id, Long schoolId);
    long countBySchoolIdAndStatus(Long schoolId, String status);
}
