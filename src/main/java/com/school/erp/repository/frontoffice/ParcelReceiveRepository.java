package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.ParcelReceive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ParcelReceiveRepository extends JpaRepository<ParcelReceive, Long> {

    Optional<ParcelReceive> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT p FROM ParcelReceive p WHERE p.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(p.senderName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.itemDetails) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.receivedBy) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.contactNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:date IS NULL OR p.dateReceived = :date) " +
            "AND (:status IS NULL OR LOWER(p.status) = LOWER(:status)) " +
            "ORDER BY p.dateReceived DESC, p.id DESC")
    Page<ParcelReceive> filterParcelReceives(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM ParcelReceive p WHERE p.school.id = :schoolId AND p.dateReceived = :date")
    long countReceivedOnDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM ParcelReceive p WHERE p.school.id = :schoolId AND p.status = 'RECEIVED'")
    long countPendingPickup(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(p) FROM ParcelReceive p WHERE p.school.id = :schoolId AND p.status = 'COLLECTED' " +
            "AND p.dateReceived >= :startDate AND p.dateReceived <= :endDate")
    long countCompletedInPeriod(
            @Param("schoolId") Long schoolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
