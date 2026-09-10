package com.school.erp.repository.frontoffice;

import com.school.erp.entity.frontoffice.VisitorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    Optional<VisitorLog> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT v FROM VisitorLog v WHERE v.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(v.visitorName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(v.meetingWith) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(v.phone) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:date IS NULL OR v.visitDate = :date) " +
            "AND (:purpose IS NULL OR LOWER(v.purpose) = LOWER(:purpose)) " +
            "AND (:status IS NULL OR LOWER(v.status) = LOWER(:status)) " +
            "ORDER BY v.visitDate DESC, v.timeIn DESC")
    Page<VisitorLog> filterVisitors(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("date") LocalDate date,
            @Param("purpose") String purpose,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(v.numberOfPeople), 0) FROM VisitorLog v WHERE v.school.id = :schoolId AND v.visitDate = :date")
    long sumGuestsForDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(v) FROM VisitorLog v WHERE v.school.id = :schoolId AND v.status = 'ON_SITE'")
    long countActiveOnSite(@Param("schoolId") Long schoolId);
}
