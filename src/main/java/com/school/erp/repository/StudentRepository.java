package com.school.erp.repository;

import com.school.erp.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolId(Long schoolId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Student s WHERE s.school.id = :schoolId AND " +
           "(CAST(:classId AS Long) IS NULL OR s.schoolClass.id = :classId) AND " +
           "(:search = '' OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    org.springframework.data.domain.Page<Student> findBySchoolIdAndSearchAndClassId(@org.springframework.data.repository.query.Param("schoolId") Long schoolId, @org.springframework.data.repository.query.Param("search") String search, @org.springframework.data.repository.query.Param("classId") Long classId, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolIdAndSectionId(Long schoolId, Long sectionId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findBySchoolIdAndSchoolClassIdAndSectionId(Long schoolId, Long classId, Long sectionId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    List<Student> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"school", "schoolClass"})
    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    long countBySchoolId(Long schoolId);

    long countByHouseId(Long houseId);

    List<Student> findBySchoolIdAndHouseId(Long schoolId, Long houseId);

    List<Student> findByFamilyId(Long familyId);

    long countByFamilyId(Long familyId);

    long countByHouseIdAndGender(Long houseId, String gender);
}
