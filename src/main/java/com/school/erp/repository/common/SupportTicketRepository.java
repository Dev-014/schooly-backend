package com.school.erp.repository;

import com.school.erp.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    
    Optional<SupportTicket> findByTicketCode(String ticketCode);

    List<SupportTicket> findBySchoolId(Long schoolId);

    List<SupportTicket> findByStatus(String status);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.assignedEmployee.id = :employeeId AND t.status != 'CLOSED'")
    long countActiveTicketsByEmployee(@Param("employeeId") Long employeeId);
}
