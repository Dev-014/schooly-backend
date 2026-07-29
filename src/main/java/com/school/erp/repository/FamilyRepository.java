package com.school.erp.repository;

import com.school.erp.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyRepository extends JpaRepository<Family, Long> {

    Optional<Family> findBySchoolIdAndFamilyCode(Long schoolId, String familyCode);

    List<Family> findBySchoolId(Long schoolId);

    Optional<Family> findByFamilyCode(String familyCode);
}
