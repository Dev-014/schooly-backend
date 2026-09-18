package com.school.erp.repository;

import com.school.erp.entity.SchoolAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolAccountRepository extends JpaRepository<SchoolAccount, Long> {
    List<SchoolAccount> findBySchoolId(Long schoolId);
}
