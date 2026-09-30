package com.school.erp.entity.library;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.school.erp.entity.superadmin.School;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "library_books")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "book_number", length = 100)
    private String bookNumber;

    @Column(name = "isbn", length = 100)
    private String isbn;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "rack_location", length = 100)
    private String rackLocation;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies = 1;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "edition", length = 100)
    private String edition;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(name = "status", nullable = false, length = 50)
    private String status = "IN STOCK";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
