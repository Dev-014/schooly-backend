package com.school.erp.repository;

import com.school.erp.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolId(Long schoolId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);
}
