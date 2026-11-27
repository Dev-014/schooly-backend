package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsEventRepository extends JpaRepository<CmsEvent, Long> {

    List<CmsEvent> findBySchoolIdOrderByIdDesc(Long schoolId);
    List<CmsEvent> findAllByOrderByIdDesc();

    @Query("SELECT e FROM CmsEvent e WHERE (:schoolId IS NULL OR e.schoolId = :schoolId) " +
           "AND (LOWER(e.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
           "OR LOWER(COALESCE(e.venue, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) ORDER BY e.id DESC")
    List<CmsEvent> searchEvents(@Param("schoolId") Long schoolId, @Param("search") String search);
}
