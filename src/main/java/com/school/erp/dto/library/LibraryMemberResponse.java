package com.school.erp.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryMemberResponse {

    private Long id;
    private Long schoolId;
    private String memberType;
    private String cardNumber;
    private Long studentId;
    private Long staffId;
    private String fullName;
    private String email;
    private String phone;
    private String classSection;
    private String admissionNumber;
    private Integer maxBooksAllowed;
    private Integer activeIssuedCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
