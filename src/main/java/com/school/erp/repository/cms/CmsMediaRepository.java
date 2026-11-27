package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsMediaRepository extends JpaRepository<CmsMedia, Long> {

    List<CmsMedia> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsMedia> findAllByOrderByIdDesc();
    List<CmsMedia> findBySchoolIdAndFileTypeIgnoreCaseOrderByIdDesc(Long schoolId, String fileType);
    List<CmsMedia> findByFileTypeIgnoreCaseOrderByIdDesc(String fileType);

    @Query("SELECT m FROM CmsMedia m WHERE (:schoolId IS NULL OR m.schoolId = :schoolId) " +
           "AND LOWER(m.fileName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "AND (:fileType IS NULL OR LOWER(COALESCE(m.fileType, '')) = LOWER(CAST(:fileType AS string))) ORDER BY m.id DESC")
    List<CmsMedia> searchMedia(@Param("schoolId") Long schoolId, @Param("search") String search, @Param("fileType") String fileType);
}
