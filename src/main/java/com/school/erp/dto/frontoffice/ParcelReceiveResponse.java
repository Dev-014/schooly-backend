package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParcelReceiveResponse {

    private Long id;
    private Long schoolId;
    private String senderName;
    private String contactNumber;
    private String recipientName;
    private String recipientType;
    private String itemDetails;
    private LocalDate dateReceived;
    private String receivedBy;
    private Long receivedByStaffId;
    private String receivedByStaffName;
    private String status; // RECEIVED, COLLECTED, RETURNED
    private LocalDateTime collectedAt;
    private String collectedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
