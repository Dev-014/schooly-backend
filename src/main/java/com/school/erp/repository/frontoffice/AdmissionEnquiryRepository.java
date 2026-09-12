package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.AdmissionEnquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionEnquiryRepository extends JpaRepository<AdmissionEnquiry, Long> {

    Optional<AdmissionEnquiry> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT e FROM AdmissionEnquiry e WHERE e.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(e.studentName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.parentName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.enquiryNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(e.phone) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:startDate IS NULL OR e.enquiryDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.enquiryDate <= :endDate) " +
            "AND (:source IS NULL OR LOWER(e.source) = LOWER(CAST(:source AS string))) " +
            "AND (:status IS NULL OR LOWER(e.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY e.enquiryDate DESC, e.id DESC")
    Page<AdmissionEnquiry> filterEnquiries(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("source") String source,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT e FROM AdmissionEnquiry e WHERE e.school.id = :schoolId " +
            "AND (:startDate IS NULL OR e.enquiryDate >= :startDate) " +
            "AND (:endDate IS NULL OR e.enquiryDate <= :endDate) " +
            "AND (:source IS NULL OR LOWER(e.source) = LOWER(CAST(:source AS string))) " +
            "AND (:status IS NULL OR LOWER(e.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY e.enquiryDate DESC, e.id DESC")
    List<AdmissionEnquiry> filterEnquiriesList(
            @Param("schoolId") Long schoolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("source") String source,
            @Param("status") String status
    );

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatusIgnoreCase(Long schoolId, String status);

    @Query("SELECT COUNT(e) FROM AdmissionEnquiry e WHERE e.school.id = :schoolId AND e.nextFollowUpDate = :date")
    long countFollowUpsDueOn(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(e) FROM AdmissionEnquiry e WHERE e.school.id = :schoolId AND e.nextFollowUpDate < :date AND LOWER(e.status) = 'active'")
    long countFollowUpsOverdue(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);
}
