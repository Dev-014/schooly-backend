package com.school.erp.repository.hr;

import com.school.erp.entity.hr.StaffBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffBankAccountRepository extends JpaRepository<StaffBankAccount, Long> {
    StaffBankAccount findByStaffId(Long staffId);
}
