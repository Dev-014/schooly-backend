package com.school.erp.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryCirculationResponse {

    private Long id;
    private Long schoolId;

    // Book info
    private Long bookId;
    private String bookTitle;
    private String bookNumber;
    private String isbn;
    private String author;
    private String coverImageUrl;

    // Member info
    private Long memberId;
    private String memberName;
    private String memberCardNumber;
    private String memberType;
    private String classSection;
    private String admissionNumber;

    // Circulation details
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String status;
    private String issuedBy;
    private String remarks;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
