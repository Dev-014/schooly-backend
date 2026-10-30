package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    @Query("SELECT p FROM CmsPage p WHERE (:schoolId IS NULL OR p.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY p.id DESC")
    List<CmsPage> searchPages(@Param("schoolId") Long schoolId, @Param("search") String search);
}

