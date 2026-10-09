package com.school.erp.repository.homework;

import com.school.erp.entity.homework.HomeworkSubmission;
import com.school.erp.entity.homework.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HomeworkSubmissionRepository extends JpaRepository<HomeworkSubmission, Long> {

    Optional<HomeworkSubmission> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<HomeworkSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    List<HomeworkSubmission> findByAssignmentIdAndSchoolId(Long assignmentId, Long schoolId);

    List<HomeworkSubmission> findByStudentIdAndSchoolId(Long studentId, Long schoolId);

    long countByAssignmentId(Long assignmentId);

    long countByAssignmentIdAndStatus(Long assignmentId, SubmissionStatus status);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.assignment.id = :assignmentId AND hs.marksObtained IS NOT NULL")
    long countEvaluatedByAssignmentId(@Param("assignmentId") Long assignmentId);

    @Query("SELECT COUNT(hs) FROM HomeworkSubmission hs WHERE hs.school.id = :schoolId AND hs.marksObtained IS NULL")
    long countPendingEvaluationBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT AVG(hs.marksObtained) FROM HomeworkSubmission hs WHERE hs.school.id = :schoolId AND hs.marksObtained IS NOT NULL")
    Double getAverageScoreBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT AVG(hs.marksObtained) FROM HomeworkSubmission hs WHERE hs.assignment.id = :assignmentId AND hs.marksObtained IS NOT NULL")
    Double getAverageScoreByAssignmentId(@Param("assignmentId") Long assignmentId);
}
