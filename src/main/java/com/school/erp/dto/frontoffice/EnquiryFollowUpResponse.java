package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnquiryFollowUpResponse {

    private Long id;
    private String actionType;
    private String notes;
    private LocalDateTime followUpDate;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
}
