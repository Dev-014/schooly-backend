package com.school.erp.repository;

import com.school.erp.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {

    List<Section> findBySchoolIdAndDeletedAtIsNull(Long schoolId);

    Optional<Section> findByIdAndSchoolIdAndDeletedAtIsNull(Long id, Long schoolId);
}
