package com.school.erp.repository;

import com.school.erp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findBySchoolId(Long schoolId);

    List<Student> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);

    List<Student> findByUserId(Long userId);
    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);
}
