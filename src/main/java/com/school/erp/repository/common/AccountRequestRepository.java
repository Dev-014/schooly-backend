package com.school.erp.repository.common;

import com.school.erp.entity.common.AccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, Long> {
    Page<AccountRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(String status);
}
