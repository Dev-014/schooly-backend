package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeAssetRepository extends JpaRepository<EmployeeAsset, Long> {
    List<EmployeeAsset> findByEmployeeId(Long employeeId);
}
