package com.school.erp.repository;

import com.school.erp.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchoolId(Long schoolId);
    List<Subject> findBySchoolIdAndGradeLevel(Long schoolId, String gradeLevel);
    Optional<Subject> findByIdAndSchoolId(Long id, Long schoolId);
    List<Subject> findBySchoolIdAndCodeStartingWith(Long schoolId, String prefix);
}
