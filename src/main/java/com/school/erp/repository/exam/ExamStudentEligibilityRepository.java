package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamStudentEligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamStudentEligibilityRepository extends JpaRepository<ExamStudentEligibility, Long> {

    List<ExamStudentEligibility> findBySchoolIdAndExamSubjectConfigId(Long schoolId, Long examSubjectConfigId);

    Optional<ExamStudentEligibility> findBySchoolIdAndExamSubjectConfigIdAndStudentId(Long schoolId, Long examSubjectConfigId, Long studentId);

    void deleteBySchoolIdAndExamSubjectConfigId(Long schoolId, Long examSubjectConfigId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM ExamStudentEligibility e WHERE e.school.id = :schoolId AND e.examSubjectConfig.examSetup.id = :examSetupId")
    List<ExamStudentEligibility> findBySchoolIdAndExamSetupId(@org.springframework.data.repository.query.Param("schoolId") Long schoolId, @org.springframework.data.repository.query.Param("examSetupId") Long examSetupId);
}
