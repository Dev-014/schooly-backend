package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsBannerRepository extends JpaRepository<CmsBanner, Long> {

    @Query("SELECT b FROM CmsBanner b WHERE (:schoolId IS NULL OR b.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(b.fileName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:fileType IS NULL OR :fileType = 'all' OR LOWER(b.fileType) = LOWER(:fileType)) ORDER BY b.id DESC")
    List<CmsBanner> searchBanners(@Param("schoolId") Long schoolId, @Param("search") String search, @Param("fileType") String fileType);
}

