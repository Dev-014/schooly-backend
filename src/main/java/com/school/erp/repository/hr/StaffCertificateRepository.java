package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffCertificateRepository extends JpaRepository<StaffCertificate, Long> {
    List<StaffCertificate> findBySchoolId(Long schoolId);
    List<StaffCertificate> findByStaffId(Long staffId);
    List<StaffCertificate> findBySchoolIdAndStaffId(Long schoolId, Long staffId);
}
