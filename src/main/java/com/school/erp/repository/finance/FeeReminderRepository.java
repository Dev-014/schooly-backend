package com.school.erp.repository.finance;

import com.school.erp.entity.finance.FeeReminder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeReminderRepository extends JpaRepository<FeeReminder, Long> {
    Page<FeeReminder> findBySchoolId(Long schoolId, Pageable pageable);
    Page<FeeReminder> findBySchoolIdAndStaffId(Long schoolId, Long staffId, Pageable pageable);
}
