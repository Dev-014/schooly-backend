package com.school.erp.repository;

import com.school.erp.entity.SuperAdminEmployee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuperAdminEmployeeRepository extends JpaRepository<SuperAdminEmployee, Long> {
    Optional<SuperAdminEmployee> findByUserId(Long userId);
    boolean existsByEmployeeCode(String employeeCode);

    @Query("SELECT e FROM SuperAdminEmployee e JOIN FETCH e.user")
    List<SuperAdminEmployee> findAllWithUser();
}
