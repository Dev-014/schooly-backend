package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.SchoolEntranceExam;
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
public interface SchoolEntranceExamRepository extends JpaRepository<SchoolEntranceExam, Long> {

    Optional<SchoolEntranceExam> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT e FROM SchoolEntranceExam e WHERE e.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(e.candidateName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.mobileNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.parentName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.examName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.centerName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:className IS NULL OR LOWER(e.className) = LOWER(CAST(:className AS string))) " +
            "AND (:status IS NULL OR LOWER(e.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY e.examDate ASC, e.createdAt DESC")
    Page<SchoolEntranceExam> filterEntranceExams(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("className") String className,
            @Param("status") String status,
            Pageable pageable
    );

    long countBySchoolId(Long schoolId);

    @Query("SELECT e.examDate FROM SchoolEntranceExam e WHERE e.school.id = :schoolId AND e.examDate >= :currentDate ORDER BY e.examDate ASC")
    List<LocalDate> findUpcomingExamDates(@Param("schoolId") Long schoolId, @Param("currentDate") LocalDate currentDate, Pageable pageable);
}
