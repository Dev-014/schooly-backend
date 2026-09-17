package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamAdmitCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamAdmitCardRepository extends JpaRepository<ExamAdmitCard, Long> {

    Optional<ExamAdmitCard> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamAdmitCard> findBySchoolIdAndExamSetupIdAndStudentId(Long schoolId, Long examSetupId, Long studentId);

    @Query("SELECT c FROM ExamAdmitCard c WHERE c.school.id = :schoolId " +
           "AND (:examSetupId IS NULL OR c.examSetup.id = :examSetupId) " +
           "AND (:classId IS NULL OR c.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR c.section.id = :sectionId) " +
           "AND (:status IS NULL OR LOWER(c.status) = LOWER(CAST(:status AS string))) " +
           "ORDER BY c.rollNumber ASC, c.student.name ASC")
    Page<ExamAdmitCard> filterAdmitCards(
            @Param("schoolId") Long schoolId,
            @Param("examSetupId") Long examSetupId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("status") String status,
            Pageable pageable);

    List<ExamAdmitCard> findBySchoolIdAndExamSetupIdAndSchoolClassId(Long schoolId, Long examSetupId, Long classId);

    long countBySchoolIdAndExamSetupId(Long schoolId, Long examSetupId);

    long countBySchoolIdAndExamSetupIdAndStatus(Long schoolId, Long examSetupId, String status);

    @Modifying
    @Query("UPDATE ExamAdmitCard c SET c.status = 'RELEASED', c.releasedAt = :releasedAt " +
           "WHERE c.school.id = :schoolId AND c.examSetup.id = :examSetupId AND c.status = 'GENERATED'")
    int releaseAllGeneratedCards(@Param("schoolId") Long schoolId,
                                 @Param("examSetupId") Long examSetupId,
                                 @Param("releasedAt") LocalDateTime releasedAt);
}
