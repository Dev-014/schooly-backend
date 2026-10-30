package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsMediaRepository extends JpaRepository<CmsMedia, Long> {

    @Query("SELECT m FROM CmsMedia m WHERE (:schoolId IS NULL OR m.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(m.fileName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:fileType IS NULL OR :fileType = 'all' OR LOWER(m.fileType) = LOWER(:fileType)) ORDER BY m.id DESC")
    List<CmsMedia> searchMedia(@Param("schoolId") Long schoolId, @Param("search") String search, @Param("fileType") String fileType);
}

