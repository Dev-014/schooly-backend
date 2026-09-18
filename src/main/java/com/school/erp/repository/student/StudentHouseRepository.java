package com.school.erp.repository;

import com.school.erp.entity.StudentHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentHouseRepository extends JpaRepository<StudentHouse, Long> {
    List<StudentHouse> findBySchoolId(Long schoolId);
    Optional<StudentHouse> findByIdAndSchoolId(Long id, Long schoolId);
}
