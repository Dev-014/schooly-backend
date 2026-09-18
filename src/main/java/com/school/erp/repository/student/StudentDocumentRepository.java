package com.school.erp.repository;

import com.school.erp.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByStudentIdAndSchoolId(Long studentId, Long schoolId);
    Optional<StudentDocument> findByIdAndSchoolId(Long id, Long schoolId);
}
