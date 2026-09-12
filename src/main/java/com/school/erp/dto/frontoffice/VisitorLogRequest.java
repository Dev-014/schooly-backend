package com.school.erp.dto.frontoffice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitorLogRequest {

    @NotBlank(message = "Visitor name is required")
    private String visitorName;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    private String meetingWith;

    private Long meetingWithStaffId;

    private String phone;

    private String email;

    private Integer numberOfPeople;

    private String idCardNumber;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    @NotNull(message = "Time in is required")
    private LocalTime timeIn;

    private LocalTime estTimeOut;

    private String note;
}
