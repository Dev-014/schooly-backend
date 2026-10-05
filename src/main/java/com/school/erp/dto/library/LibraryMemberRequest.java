package com.school.erp.dto.library;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryMemberRequest {

    private String memberType;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    private Long studentId;
    private Long staffId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;
    private String phone;
    private String classSection;
    private String admissionNumber;
    private Integer maxBooksAllowed;
    private String status;
}
