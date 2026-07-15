package com.school.erp.repository;

import com.school.erp.entity.StudentLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentLeaveRepository extends JpaRepository<StudentLeave, Long> {

    List<StudentLeave> findByStudentIdAndDeletedAtIsNull(Long studentId);

    List<StudentLeave> findBySchoolIdAndDeletedAtIsNull(Long schoolId);

    List<StudentLeave> findBySchoolIdAndStatusAndDeletedAtIsNull(Long schoolId, String status);

    Optional<StudentLeave> findByIdAndSchoolIdAndDeletedAtIsNull(Long id, Long schoolId);
}
