package com.school.erp.repository;

import com.school.erp.entity.ClassSubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassSubjectAssignmentRepository extends JpaRepository<ClassSubjectAssignment, Long> {
    List<ClassSubjectAssignment> findBySchoolIdAndSchoolClassIdAndAcademicYearId(Long schoolId, Long classId, Long academicYearId);
    List<ClassSubjectAssignment> findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearId(Long schoolId, Long classId, Long sectionId, Long academicYearId);
    List<ClassSubjectAssignment> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    Optional<ClassSubjectAssignment> findBySchoolIdAndSchoolClassIdAndSectionIdAndSubjectIdAndAcademicYearId(Long schoolId, Long classId, Long sectionId, Long subjectId, Long academicYearId);
    void deleteBySchoolIdAndSchoolClassIdAndAcademicYearId(Long schoolId, Long classId, Long academicYearId);
}
