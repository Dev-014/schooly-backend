package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamDivision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamDivisionRepository extends JpaRepository<ExamDivision, Long> {

    Optional<ExamDivision> findByIdAndSchoolId(Long id, Long schoolId);

    List<ExamDivision> findBySchoolIdOrderByPercentFromDesc(Long schoolId);

    long countBySchoolId(Long schoolId);
}
