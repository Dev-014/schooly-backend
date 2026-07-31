package com.school.erp.repository.communication;

import com.school.erp.entity.communication.CommunicationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunicationTemplateRepository extends JpaRepository<CommunicationTemplate, Long> {
    Page<CommunicationTemplate> findByIsActiveTrue(Pageable pageable);
}
