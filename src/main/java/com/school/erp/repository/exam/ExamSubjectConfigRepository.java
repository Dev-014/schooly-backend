package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamSubjectConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSubjectConfigRepository extends JpaRepository<ExamSubjectConfig, Long> {

    List<ExamSubjectConfig> findBySchoolIdAndExamSetupId(Long schoolId, Long examSetupId);

    Optional<ExamSubjectConfig> findBySchoolIdAndExamSetupIdAndSubjectId(Long schoolId, Long examSetupId, Long subjectId);

    boolean existsBySchoolIdAndExamSetupIdAndSubjectId(Long schoolId, Long examSetupId, Long subjectId);
}
