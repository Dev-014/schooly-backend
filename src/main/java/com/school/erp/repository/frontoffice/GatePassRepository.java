package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.GatePass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    Optional<GatePass> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT g FROM GatePass g WHERE g.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(g.personName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(g.passNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(g.classOrDepartment) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(g.reasonForExit) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:date IS NULL OR g.passDate = :date) " +
            "AND (:role IS NULL OR LOWER(g.role) = LOWER(CAST(:role AS string))) " +
            "AND (:status IS NULL OR LOWER(g.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY g.passDate DESC, g.exitTime DESC")
    Page<GatePass> filterGatePasses(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("role") String role,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(g) FROM GatePass g WHERE g.school.id = :schoolId AND g.passDate = :date")
    long countIssuedOnDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(g) FROM GatePass g WHERE g.school.id = :schoolId AND g.passDate = :date AND g.status = 'APPROVED'")
    long countApprovedOnDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);
}
