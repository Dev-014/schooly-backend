package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.ParcelDispatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ParcelDispatchRepository extends JpaRepository<ParcelDispatch, Long> {

    Optional<ParcelDispatch> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT p FROM ParcelDispatch p WHERE p.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(p.receiverName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.receiverInstitution) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.itemDetails) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.trackingNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(p.courierName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:date IS NULL OR p.dispatchDate = :date) " +
            "AND (:status IS NULL OR LOWER(p.status) = LOWER(:status)) " +
            "ORDER BY p.dispatchDate DESC, p.id DESC")
    Page<ParcelDispatch> filterParcelDispatches(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM ParcelDispatch p WHERE p.school.id = :schoolId AND p.dispatchDate = :date")
    long countDispatchedOnDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(p) FROM ParcelDispatch p WHERE p.school.id = :schoolId AND p.status IN ('IN_TRANSIT', 'DELAYED')")
    long countPendingDelivery(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(DISTINCT p.courierName) FROM ParcelDispatch p WHERE p.school.id = :schoolId AND p.status IN ('IN_TRANSIT', 'DELAYED')")
    long countCouriersWithPendingDeliveries(@Param("schoolId") Long schoolId);

    @Query("SELECT COUNT(p) FROM ParcelDispatch p WHERE p.school.id = :schoolId AND p.status = 'DELIVERED' " +
            "AND p.dispatchDate >= :startDate AND p.dispatchDate <= :endDate")
    long countDeliveredInPeriod(
            @Param("schoolId") Long schoolId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
