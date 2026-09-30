package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.SchoolComplaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SchoolComplaintRepository extends JpaRepository<SchoolComplaint, Long> {

    Optional<SchoolComplaint> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT c FROM SchoolComplaint c WHERE c.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(c.complainantName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(c.mobileNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:complaintType IS NULL OR LOWER(c.complaintType) = LOWER(CAST(:complaintType AS string))) " +
            "AND (:status IS NULL OR LOWER(c.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY c.complaintDate DESC, c.createdAt DESC")
    Page<SchoolComplaint> filterComplaints(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("complaintType") String complaintType,
            @Param("status") String status,
            Pageable pageable
    );

    long countBySchoolId(Long schoolId);

    @Query("SELECT COUNT(c) FROM SchoolComplaint c WHERE c.school.id = :schoolId AND LOWER(c.status) IN ('open', 'pending')")
    long countOpenComplaints(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(c) FROM SchoolComplaint c WHERE c.school.id = :schoolId AND LOWER(c.status) IN ('closed', 'resolved')")
    long countClosedComplaints(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(c) FROM SchoolComplaint c WHERE c.school.id = :schoolId " +
            "AND LOWER(c.status) IN ('closed', 'resolved') " +
            "AND c.complaintDate >= :startDate AND c.complaintDate <= :endDate")
    long countResolvedBetweenDates(
            @Param("schoolId") Long schoolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
