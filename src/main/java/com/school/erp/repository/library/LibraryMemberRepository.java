package com.school.erp.repository.library;

import com.school.erp.entity.library.LibraryMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LibraryMemberRepository extends JpaRepository<LibraryMember, Long> {

    Optional<LibraryMember> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<LibraryMember> findBySchoolIdAndCardNumber(Long schoolId, String cardNumber);

    @Query("SELECT m FROM LibraryMember m WHERE m.school.id = :schoolId " +
            "AND (:search IS NULL OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(m.cardNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(m.admissionNumber) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(m.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "     OR LOWER(m.classSection) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:memberType IS NULL OR LOWER(m.memberType) = LOWER(CAST(:memberType AS string))) " +
            "AND (:status IS NULL OR LOWER(m.status) = LOWER(CAST(:status AS string))) " +
            "ORDER BY m.fullName ASC")
    Page<LibraryMember> filterMembers(
            @Param("schoolId") Long schoolId,
            @Param("search") String search,
            @Param("memberType") String memberType,
            @Param("status") String status,
            Pageable pageable
    );

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatusIgnoreCase(Long schoolId, String status);
}
