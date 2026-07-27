package com.school.erp.repository;

import com.school.erp.entity.StudentSubjectEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentSubjectEnrollmentRepository extends JpaRepository<StudentSubjectEnrollment, Long> {
    List<StudentSubjectEnrollment> findBySchoolIdAndStudentId(Long schoolId, Long studentId);
    List<StudentSubjectEnrollment> findBySchoolIdAndStudentIdAndAcademicYearId(Long schoolId, Long studentId, Long academicYearId);
    List<StudentSubjectEnrollment> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    Optional<StudentSubjectEnrollment> findBySchoolIdAndStudentIdAndSubjectIdAndAcademicYearId(Long schoolId, Long studentId, Long subjectId, Long academicYearId);
    void deleteBySchoolIdAndStudentIdAndAcademicYearId(Long schoolId, Long studentId, Long academicYearId);
    long countBySchoolIdAndAcademicYearIdAndEnrollmentType(Long schoolId, Long academicYearId, String enrollmentType);
    long countBySchoolIdAndAcademicYearIdAndStatus(Long schoolId, Long academicYearId, String status);
}
