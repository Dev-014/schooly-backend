package com.school.erp.dto.library;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBookRequest {

    @NotBlank(message = "Book title is required")
    private String title;

    private String bookNumber;
    private String isbn;

    @NotBlank(message = "Author is required")
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
}
