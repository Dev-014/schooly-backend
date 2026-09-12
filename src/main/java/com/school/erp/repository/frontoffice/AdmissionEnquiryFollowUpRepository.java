package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.AdmissionEnquiryFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmissionEnquiryFollowUpRepository extends JpaRepository<AdmissionEnquiryFollowUp, Long> {

    List<AdmissionEnquiryFollowUp> findByEnquiryIdOrderByFollowUpDateDesc(Long enquiryId);
}
