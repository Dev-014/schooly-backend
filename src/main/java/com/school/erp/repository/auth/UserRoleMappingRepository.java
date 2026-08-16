package com.school.erp.repository.auth;

import com.school.erp.entity.auth.UserRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleMappingRepository extends JpaRepository<UserRoleMapping, Long> {
    List<UserRoleMapping> findBySchoolIdAndUserIdAndIsActiveTrue(Long schoolId, Long userId);
}
