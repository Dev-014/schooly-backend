package com.school.erp.repository.homework;

import com.school.erp.entity.homework.AssignmentStatus;
import com.school.erp.entity.homework.AssignmentType;
import com.school.erp.entity.homework.HomeworkAssignment;
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
public interface HomeworkAssignmentRepository extends JpaRepository<HomeworkAssignment, Long> {

    Optional<HomeworkAssignment> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("""
        SELECT ha FROM HomeworkAssignment ha
        WHERE ha.school.id = :schoolId
          AND (:assignmentType IS NULL OR ha.assignmentType = :assignmentType)
          AND (:classId IS NULL OR ha.schoolClass.id = :classId)
          AND (:sectionId IS NULL OR ha.section.id = :sectionId)
          AND (:subjectId IS NULL OR ha.subject.id = :subjectId)
          AND (:status IS NULL OR ha.status = :status)
          AND (:startDate IS NULL OR ha.assignedDate >= :startDate)
          AND (:endDate IS NULL OR ha.dueDate <= :endDate)
          AND (:search IS NULL OR LOWER(ha.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
               OR LOWER(ha.subject.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
        ORDER BY ha.assignedDate DESC, ha.id DESC
    """)
    Page<HomeworkAssignment> filterAssignments(
            @Param("schoolId") Long schoolId,
            @Param("assignmentType") AssignmentType assignmentType,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId,
            @Param("status") AssignmentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT ha FROM HomeworkAssignment ha
        WHERE ha.school.id = :schoolId
          AND ha.schoolClass.id = :classId
          AND (ha.section.id IS NULL OR ha.section.id = :sectionId)
          AND (:assignmentType IS NULL OR ha.assignmentType = :assignmentType)
          AND ha.status = 'PUBLISHED'
        ORDER BY ha.dueDate ASC, ha.id DESC
    """)
    List<HomeworkAssignment> findActiveForClassAndSection(
            @Param("schoolId") Long schoolId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("assignmentType") AssignmentType assignmentType
    );

    long countBySchoolIdAndAssignmentType(Long schoolId, AssignmentType assignmentType);

    @Query("""
        SELECT COUNT(ha) FROM HomeworkAssignment ha
        WHERE ha.school.id = :schoolId
          AND ha.schoolClass.id = :classId
          AND (:sectionId IS NULL OR ha.section.id = :sectionId)
          AND ha.subject.id = :subjectId
          AND ha.assignedDate = :date
    """)
    long countAssignmentsOnDate(
            @Param("schoolId") Long schoolId,
            @Param("classId") Long classId,
            @Param("sectionId") Long sectionId,
            @Param("subjectId") Long subjectId,
            @Param("date") LocalDate date
    );

    @Query("""
        SELECT DISTINCT ha.schoolClass.id, ha.section.id, ha.subject.id
        FROM HomeworkAssignment ha
        WHERE ha.school.id = :schoolId
          AND ha.assignedDate = :date
    """)
    List<Object[]> findAssignedClassSectionSubjectTuples(
            @Param("schoolId") Long schoolId,
            @Param("date") LocalDate date
    );
}
