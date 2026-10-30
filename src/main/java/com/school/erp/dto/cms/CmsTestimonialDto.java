package com.school.erp.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTestimonialDto {
    private Long id;
    private Long schoolId;
    private String name;
    private String designation;
    private String testimonial;
    private String fullTestimonial;
    private String image;
    private Integer rating;
    private String date;
}

