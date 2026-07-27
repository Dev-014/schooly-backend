package com.school.erp.repository;

import com.school.erp.entity.ClassTeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassTeacherAssignmentRepository extends JpaRepository<ClassTeacherAssignment, Long> {
    List<ClassTeacherAssignment> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    List<ClassTeacherAssignment> findBySchoolIdAndAcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);
    Optional<ClassTeacherAssignment> findBySchoolIdAndSectionIdAndAcademicYearIdAndStatus(Long schoolId, Long sectionId, Long academicYearId, String status);
    Optional<ClassTeacherAssignment> findByIdAndSchoolId(Long id, Long schoolId);
    List<ClassTeacherAssignment> findBySchoolIdAndStatus(Long schoolId, String status);
}
