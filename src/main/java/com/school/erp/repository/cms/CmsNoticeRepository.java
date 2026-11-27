package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsNoticeRepository extends JpaRepository<CmsNotice, Long> {

    List<CmsNotice> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsNotice> findAllByOrderByIdDesc();

    @Query("SELECT n FROM CmsNotice n WHERE (:schoolId IS NULL OR n.schoolId = :schoolId) " +
           "AND (LOWER(n.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(COALESCE(n.description, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) ORDER BY n.id DESC")
    List<CmsNotice> searchNotices(@Param("schoolId") Long schoolId, @Param("search") String search);
}
