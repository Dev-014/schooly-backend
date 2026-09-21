package com.school.erp.repository.student;

import com.school.erp.entity.student.StudentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCategoryRepository extends JpaRepository<StudentCategory, Long> {
    List<StudentCategory> findBySchoolId(Long schoolId);
    Optional<StudentCategory> findByIdAndSchoolId(Long id, Long schoolId);
}
