package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.IncomeHead;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeHeadRepository extends JpaRepository<IncomeHead, Long> {
    List<IncomeHead> findBySchool(School school);
    Optional<IncomeHead> findByIdAndSchool(Long id, School school);
}
