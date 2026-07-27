package com.school.erp.repository;

import com.school.erp.entity.TimetablePeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetablePeriodRepository extends JpaRepository<TimetablePeriod, Long> {
    List<TimetablePeriod> findBySchoolIdOrderByPeriodNumberAsc(Long schoolId);
    Optional<TimetablePeriod> findByIdAndSchoolId(Long id, Long schoolId);
}
