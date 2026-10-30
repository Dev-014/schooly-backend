package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsNoticeRepository extends JpaRepository<CmsNotice, Long> {

    @Query("SELECT n FROM CmsNotice n WHERE (:schoolId IS NULL OR n.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(n.description) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY n.id DESC")
    List<CmsNotice> searchNotices(@Param("schoolId") Long schoolId, @Param("search") String search);
}

