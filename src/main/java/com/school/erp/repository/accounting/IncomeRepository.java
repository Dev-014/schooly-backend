package com.school.erp.repository.accounting;

import com.school.erp.entity.accounting.Income;
import com.school.erp.entity.superadmin.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findBySchool(School school);
    Optional<Income> findByIdAndSchool(Long id, School school);
}
