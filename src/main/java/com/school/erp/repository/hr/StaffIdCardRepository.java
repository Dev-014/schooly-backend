package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffIdCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffIdCardRepository extends JpaRepository<StaffIdCard, Long> {
    List<StaffIdCard> findBySchoolId(Long schoolId);
    List<StaffIdCard> findByStaffId(Long staffId);
}
