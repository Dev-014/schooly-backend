package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsEventRepository extends JpaRepository<CmsEvent, Long> {

    @Query("SELECT e FROM CmsEvent e WHERE (:schoolId IS NULL OR e.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.venue) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY e.id DESC")
    List<CmsEvent> searchEvents(@Param("schoolId") Long schoolId, @Param("search") String search);
}

