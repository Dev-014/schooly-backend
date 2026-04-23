package com.school.erp.repository;

import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserSchoolRoleRepository extends JpaRepository<UserSchoolRole, Long> {

    List<UserSchoolRole> findByUserIdAndStatusIgnoreCase(Long userId, String status);

    Optional<UserSchoolRole> findByUserIdAndSchoolIdAndStatusIgnoreCase(Long userId, Long schoolId, String status);

    boolean existsByUserIdAndSchoolIdAndRoleAndStatusIgnoreCase(Long userId, Long schoolId, UserRole role, String status);
}
