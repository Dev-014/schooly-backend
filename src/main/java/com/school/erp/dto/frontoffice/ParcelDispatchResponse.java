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
public class ParcelDispatchResponse {

    private Long id;
    private Long schoolId;
    private String receiverName;
    private String receiverInstitution;
    private String deliveryAddress;
    private String phoneNumber;
    private LocalDate dispatchDate;
    private String itemDetails;
    private String courierName;
    private String trackingNumber;
    private String status; // IN_TRANSIT, DELIVERED, DELAYED, CANCELLED
    private LocalDate deliveredDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
