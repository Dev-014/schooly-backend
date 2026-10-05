package com.school.erp.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBookResponse {

    private Long id;
    private Long schoolId;
    private String title;
    private String bookNumber;
    private String isbn;
    private String author;
    private String publisher;
    private String category;
    private String rackLocation;
    private Integer quantity;
    private Integer availableCopies;
    private BigDecimal price;
    private String edition;
    private String description;
    private String coverImageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
