package com.school.erp.repository.cms;

import com.school.erp.entity.cms.CmsTestimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CmsTestimonialRepository extends JpaRepository<CmsTestimonial, Long> {

    @Query("SELECT t FROM CmsTestimonial t WHERE (:schoolId IS NULL OR t.schoolId = :schoolId) " +
           "AND (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.designation) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.testimonial) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY t.id DESC")
    List<CmsTestimonial> searchTestimonials(@Param("schoolId") Long schoolId, @Param("search") String search);
}

