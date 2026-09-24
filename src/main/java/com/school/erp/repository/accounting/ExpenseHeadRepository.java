package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.ExpenseHead;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseHeadRepository extends JpaRepository<ExpenseHead, Long> {
    List<ExpenseHead> findBySchool(School school);
    Optional<ExpenseHead> findByIdAndSchool(Long id, School school);
}
