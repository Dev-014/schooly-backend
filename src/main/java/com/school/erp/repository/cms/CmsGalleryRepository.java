package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsGalleryRepository extends JpaRepository<CmsGallery, Long> {

    List<CmsGallery> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsGallery> findAllByOrderByIdDesc();

    @Query("SELECT g FROM CmsGallery g WHERE (:schoolId IS NULL OR g.schoolId = :schoolId) " +
           "AND (LOWER(g.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(COALESCE(g.description, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) ORDER BY g.id DESC")
    List<CmsGallery> searchGalleries(@Param("schoolId") Long schoolId, @Param("search") String search);
}
