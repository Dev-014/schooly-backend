package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamApplicability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamApplicabilityRepository extends JpaRepository<ExamApplicability, Long> {

    List<ExamApplicability> findBySchoolIdAndExamSetupId(Long schoolId, Long examSetupId);
    
    Optional<ExamApplicability> findBySchoolIdAndExamSetupIdAndSchoolClassIdAndSectionId(Long schoolId, Long examSetupId, Long classId, Long sectionId);

    int countBySchoolIdAndExamSetupId(Long schoolId, Long examSetupId);
}
