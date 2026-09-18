package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.SchoolSubscriptionInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolSubscriptionInstallmentRepository extends JpaRepository<SchoolSubscriptionInstallment, Long> {
    List<SchoolSubscriptionInstallment> findBySubscriptionIdOrderByInstallmentNumberAsc(Long subscriptionId);
}
