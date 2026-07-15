package com.school.erp.repository;

import com.school.erp.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Page<Student> findBySchoolIdAndDeletedAtIsNull(Long schoolId, Pageable pageable);

    Page<Student> findBySchoolIdAndSchoolClassIdAndDeletedAtIsNull(Long schoolId, Long classId, Pageable pageable);

    List<Student> findBySchoolIdAndDeletedAtIsNull(Long schoolId);

    List<Student> findBySchoolIdAndSchoolClassIdAndDeletedAtIsNull(Long schoolId, Long classId);

    Optional<Student> findByIdAndSchoolIdAndDeletedAtIsNull(Long id, Long schoolId);

    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    boolean existsByAdmissionNoAndSchoolId(String admissionNo, Long schoolId);

    boolean existsByAdmissionNoAndSchoolIdAndIdNot(String admissionNo, Long schoolId, Long id);

    Optional<Student> findByAdmissionNoAndSchoolIdAndDeletedAtIsNull(String admissionNo, Long schoolId);

    List<Student> findByNameContainingIgnoreCaseAndSchoolIdAndDeletedAtIsNull(String name, Long schoolId);

    List<Student> findBySchoolIdAndStatusAndDeletedAtIsNull(Long schoolId, String status);

    Page<Student> findBySchoolId(Long schoolId, Pageable pageable);

    Page<Student> findBySchoolIdAndStatusAndDeletedAtIsNull(Long schoolId, String status, Pageable pageable);

    Page<Student> findBySchoolIdAndSchoolClassIdAndStatusAndDeletedAtIsNull(Long schoolId, Long classId, String status, Pageable pageable);

    long countBySchoolIdAndDeletedAtIsNull(Long schoolId);

    long countBySchoolId(Long schoolId);
}
