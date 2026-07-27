package com.school.erp.repository;

import com.school.erp.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findBySchoolId(Long schoolId);
    List<Section> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);
    Optional<Section> findByIdAndSchoolId(Long id, Long schoolId);
}
