package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CmsSettingRepository extends JpaRepository<CmsSetting, Long> {

    Optional<CmsSetting> findFirstBySchoolIdOrderByIdAsc(Long schoolId);

    Optional<CmsSetting> findFirstByOrderByIdAsc();
}

