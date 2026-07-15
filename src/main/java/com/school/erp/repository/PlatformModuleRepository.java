package com.school.erp.repository;

import com.school.erp.entity.PlatformModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformModuleRepository extends JpaRepository<PlatformModule, Long> {
    Optional<PlatformModule> findByCode(String code);
}
