package com.school.erp.repository.auth;

import com.school.erp.entity.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    List<Role> findBySchoolId(Long schoolId);
    Optional<Role> findByIdAndSchoolId(String id, Long schoolId);
}
