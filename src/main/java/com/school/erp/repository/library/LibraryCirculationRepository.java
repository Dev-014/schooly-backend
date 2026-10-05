package com.school.erp.repository.library;

import com.school.erp.entity.library.LibraryCirculation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface LibraryCirculationRepository extends JpaRepository<LibraryCirculation, Long> {

    Optional<LibraryCirculation> findByIdAndSchoolId(Long id, Long schoolId);

    @Query("SELECT c FROM LibraryCirculation c WHERE c.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(c.book.title) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(c.member.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(c.member.cardNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(c.book.isbn) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:status IS NULL OR LOWER(c.status) = LOWER(CAST(:status AS string))) " +
            "AND (:memberId IS NULL OR c.member.id = :memberId) " +
            "AND (:bookId IS NULL OR c.book.id = :bookId) " +
            "ORDER BY c.issueDate DESC, c.createdAt DESC")
    Page<LibraryCirculation> filterCirculations(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("status") String status,
            @Param("memberId") Long memberId,
            @Param("bookId") Long bookId,
            Pageable pageable
    );

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatusIgnoreCase(Long schoolId, String status);

    @Query("SELECT COUNT(c) FROM LibraryCirculation c WHERE c.school.id = :schoolId AND LOWER(c.status) = 'issued' AND c.dueDate < :today")
    long countOverdue(
            @Param("schoolId") Long schoolId,
            @Param("today") LocalDate today
    );

    @Query("SELECT COALESCE(SUM(c.fineAmount), 0) FROM LibraryCirculation c WHERE c.school.id = :schoolId AND c.finePaid = true")
    BigDecimal sumFinesCollected(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(c.fineAmount), 0) FROM LibraryCirculation c WHERE c.school.id = :schoolId AND c.finePaid = false AND c.fineAmount > 0")
    BigDecimal sumFinesPending(@Param("schoolId") Long schoolId);
}
