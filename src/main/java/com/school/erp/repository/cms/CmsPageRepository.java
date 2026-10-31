package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    List<CmsPage> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsPage> findAllByOrderByIdDesc();

    @Query("SELECT p FROM CmsPage p WHERE (:schoolId IS NULL OR p.schoolId = :schoolId) " +
           "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) ORDER BY p.id DESC")
    List<CmsPage> searchPages(@Param("schoolId") Long schoolId, @Param("search") String search);

    boolean existsByTitleIgnoreCase(String title);
}

