package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsBannerRepository extends JpaRepository<CmsBanner, Long> {

    List<CmsBanner> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsBanner> findAllByOrderByIdDesc();
    List<CmsBanner> findBySchoolIdAndFileTypeIgnoreCaseOrderByIdDesc(Long schoolId, String fileType);
    List<CmsBanner> findByFileTypeIgnoreCaseOrderByIdDesc(String fileType);

    @Query("SELECT b FROM CmsBanner b WHERE (:schoolId IS NULL OR b.schoolId = :schoolId) " +
           "AND LOWER(b.fileName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "AND (:fileType IS NULL OR LOWER(COALESCE(b.fileType, '')) = LOWER(CAST(:fileType AS string))) ORDER BY b.id DESC")
    List<CmsBanner> searchBanners(@Param("schoolId") Long schoolId, @Param("search") String search, @Param("fileType") String fileType);
}
