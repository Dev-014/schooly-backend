package com.school.erp.repository.superadmin;

import com.school.erp.entity.superadmin.EmployeeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeNoteRepository extends JpaRepository<EmployeeNote, Long> {
    List<EmployeeNote> findByEmployeeId(Long employeeId);
}
