package com.school.erp.repository.finance;

import com.school.erp.entity.finance.GatewayTransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GatewayTransactionLogRepository extends JpaRepository<GatewayTransactionLog, Long> {

    Optional<GatewayTransactionLog> findByGatewayOrderId(String gatewayOrderId);

    Optional<GatewayTransactionLog> findByGatewayPaymentId(String gatewayPaymentId);

    Page<GatewayTransactionLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

    Page<GatewayTransactionLog> findByDomainAndReferenceIdOrderByCreatedAtDesc(String domain, Long referenceId, Pageable pageable);
}
