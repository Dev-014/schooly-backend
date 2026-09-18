package com.school.erp.repository.finance;

import com.school.erp.entity.finance.SchoolAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolAccountRepository extends JpaRepository<SchoolAccount, Long> {
    List<SchoolAccount> findBySchoolId(Long schoolId);
}
