package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    Optional<ExamSchedule> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT s FROM ExamSchedule s WHERE s.school.id = :schoolId " +
           "AND (:examSetupId IS NULL OR s.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR s.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR s.section.id = :sectionId) " +
           "AND (:search IS NULL OR LOWER(s.subject.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(s.subject.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "ORDER BY s.examDate ASC, s.startTime ASC")
    Page<ExamSchedule> filterSchedules(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("search") String search,
            Pageable pageable);

    List<ExamSchedule> findBySchoolIdAndExamSetupIdAndSchoolClassIdOrderByExamDateAscStartTimeAsc(
            Long schoolId, Long examSetupId, Long classId);

    long countBySchoolIdAndExamDateGreaterThanEqual(Long schoolId, LocalDate fromDate);

    @Query("SELECT COUNT(DISTINCT s.subject.id) FROM ExamSchedule s WHERE s.school.id = :schoolId")
    long countDistinctSubjectsBySchoolId(@Param("schoolId") Long schoolId);

    Optional<ExamSchedule> findBySchoolIdAndExamSetupIdAndSchoolClassIdAndSectionIdAndSubjectId(
            Long schoolId, Long examSetupId, Long classId, Long sectionId, Long subjectId);
}
