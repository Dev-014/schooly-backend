package com.school.erp.repository.student;

import com.school.erp.entity.student.IdCardGeneration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IdCardGenerationRepository extends JpaRepository<IdCardGeneration, Long> {
    List<IdCardGeneration> findBySchoolId(Long schoolId);
    Optional<IdCardGeneration> findByStudentIdAndSchoolId(Long studentId, Long schoolId);
}
