package com.school.erp.repository;

import com.school.erp.entity.FeeDue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeeDueRepository extends JpaRepository<FeeDue, Long> {
    List<FeeDue> findByStudentIdAndSchoolIdOrderByDueDateAsc(Long studentId, Long schoolId);
    List<FeeDue> findByStudentIdAndSchoolIdAndStatusInOrderByDueDateAsc(Long studentId, Long schoolId, List<String> statuses);
}
