package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Expense;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findBySchool(School school);
    Optional<Expense> findByIdAndSchool(Long id, School school);
}
