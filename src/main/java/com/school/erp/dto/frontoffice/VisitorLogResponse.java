package com.school.erp.dto.frontoffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorLogResponse {

    private Long id;
    private Long schoolId;
    private String visitorName;
    private String purpose;
    private String meetingWith;
    private Long meetingWithStaffId;
    private String meetingWithStaffName;
    private String phone;
    private String email;
    private Integer numberOfPeople;
    private String idCardNumber;
    private LocalDate visitDate;
    private LocalTime timeIn;
    private LocalTime estTimeOut;
    private LocalTime timeOut;
    private String status; // ON_SITE, CHECKED_OUT
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
