package com.school.erp.repository;

import com.school.erp.entity.StudentFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentFeeStructureRepository extends JpaRepository<StudentFeeStructure, Long> {
    List<StudentFeeStructure> findByStudentId(Long studentId);
}
