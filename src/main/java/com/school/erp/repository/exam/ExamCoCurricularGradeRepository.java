package com.school.erp.repository.exam;

import com.school.erp.entity.exam.ExamCoCurricularGrade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamCoCurricularGradeRepository extends JpaRepository<ExamCoCurricularGrade, Long> {

    Optional<ExamCoCurricularGrade> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<ExamCoCurricularGrade> findBySchoolIdAndTermIdAndStudentId(Long schoolId, Long termId, Long studentId);

    @Query("SELECT g FROM ExamCoCurricularGrade g " +
           "WHERE g.school.id = :schoolId " +
           "AND g.term.id = :termId " +
           "AND g.student.id IN :studentIds")
    List<ExamCoCurricularGrade> findBySchoolIdAndTermIdAndStudentIdIn(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("studentIds") java.util.Collection<Long> studentIds);

    @Query(value = "SELECT g FROM ExamCoCurricularGrade g " +
           "JOIN FETCH g.student s " +
           "JOIN FETCH g.schoolClass sc " +
           "LEFT JOIN FETCH g.section sec " +
           "JOIN FETCH g.term t " +
           "WHERE g.school.id = :schoolId " +
           "AND (:termId IS NULL OR g.term.id = :termId) " +
           "AND (:classId IS NULL OR g.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR g.section.id = :sectionId) " +
           "AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(s.admissionNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "ORDER BY s.rollNumber ASC, s.name ASC",
           countQuery = "SELECT count(g) FROM ExamCoCurricularGrade g WHERE g.school.id = :schoolId " +
           "AND (:termId IS NULL OR g.term.id = :termId) " +
           "AND (:classId IS NULL OR g.schoolClass.id = :classId) " +
           "AND (:sectionId IS NULL OR g.section.id = :sectionId) " +
           "AND (:search IS NULL OR LOWER(g.student.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(g.student.admissionNo) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "     OR LOWER(g.student.rollNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))")
    Page<ExamCoCurricularGrade> filterCoCurricular(
            @Param("schoolId") Long schoolId,
            @Param("termId") Long termId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("search") String search,
            Pageable pageable);

    List<ExamCoCurricularGrade> findBySchoolIdAndTermIdAndSchoolClassId(Long schoolId, Long termId, Long classId);

    long countBySchoolIdAndTermId(Long schoolId, Long termId);

    long countBySchoolIdAndTermIdAndStatus(Long schoolId, Long termId, String status);
}
