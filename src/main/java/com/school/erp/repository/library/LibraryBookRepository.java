package com.school.erp.repository.library;

import com.school.erp.entity.library.LibraryBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {

    Optional<LibraryBook> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT b FROM LibraryBook b WHERE b.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(b.author) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(b.bookNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(b.rackLocation) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:category IS NULL OR LOWER(b.category) = LOWER(CAST(:category AS string))) " +
            "AND (:status IS NULL OR LOWER(b.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY b.title ASC")
    Page<LibraryBook> filterBooks(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("status") String status,
            Pageable pageable
    );

    long countBySchoolId(Long schoolId);

    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM LibraryBook b WHERE b.school.id = :schoolId")
    long sumTotalQuantity(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(b.availableCopies), 0) FROM LibraryBook b WHERE b.school.id = :schoolId")
    long sumAvailableCopies(@Param("schoolId") Long schoolId);
}
