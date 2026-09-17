package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamGradeScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamGradeScaleRepository extends JpaRepository<ExamGradeScale, Long> {

    Optional<ExamGradeScale> findByIdAndSchoolId(Long id, Long schoolId);

    List<ExamGradeScale> findBySchoolIdOrderByPercentFromDesc(Long schoolId);

    List<ExamGradeScale> findBySchoolIdAndTargetClassOrderByPercentFromDesc(Long schoolId, String targetClass);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, String status);
}
