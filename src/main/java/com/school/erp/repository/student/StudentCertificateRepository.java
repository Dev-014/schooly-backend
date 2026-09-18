package com.school.erp.repository;

import com.school.erp.entity.StudentCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, Long> {
    List<StudentCertificate> findBySchoolId(Long schoolId);
    Optional<StudentCertificate> findByIdAndSchoolId(Long id, Long schoolId);
}
