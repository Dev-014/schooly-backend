package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsMenuGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsMenuGroupRepository extends JpaRepository<CmsMenuGroup, Long> {

    List<CmsMenuGroup> findBySchoolIdOrderByIdAsc(Long schoolId);
    List<CmsMenuGroup> findAllByOrderByIdAsc();

    @Query("SELECT m FROM CmsMenuGroup m WHERE (:schoolId IS NULL OR m.schoolId = :schoolId) " +
           "AND LOWER(m.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) ORDER BY m.id ASC")
    List<CmsMenuGroup> searchMenuGroups(@Param("schoolId") Long schoolId, @Param("search") String search);
}
