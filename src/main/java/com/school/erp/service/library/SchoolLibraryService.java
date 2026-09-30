package com.school.erp.service.library;

import com.school.erp.dto.library.*;
import com.school.erp.entity.library.LibraryBook;
import com.school.erp.entity.library.LibraryCirculation;
import com.school.erp.entity.library.LibraryMember;
import com.school.erp.entity.superadmin.School;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.hr.Staff;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.library.LibraryBookRepository;
import com.school.erp.repository.library.LibraryCirculationRepository;
import com.school.erp.repository.library.LibraryMemberRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.security.AuthContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SchoolLibraryService {

    private final AuthContextService authContextService;
    private final LibraryBookRepository bookRepository;
    private final LibraryMemberRepository memberRepository;
    private final LibraryCirculationRepository circulationRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    // ─── Books ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LibraryBookResponse> filterBooks(
            Long rawSchoolId,
            String search,
            String category,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanCat = (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")) ? category.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<LibraryBook> page = bookRepository.filterBooks(schoolId, cleanSearch, cleanCat, cleanStatus, pageable);
        return page.map(this::mapBookToResponse);
    }

    @Transactional(readOnly = true)
    public LibraryBookResponse getBookById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryBook book = bookRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return mapBookToResponse(book);
    }

    @Transactional
    public LibraryBookResponse createBook(Long rawSchoolId, LibraryBookRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        LibraryBook book = new LibraryBook();
        book.setSchool(school);
        book.setTitle(request.getTitle());
        book.setBookNumber(request.getBookNumber());
        book.setIsbn(request.getIsbn());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setCategory(request.getCategory());
        book.setRackLocation(request.getRackLocation());

        int qty = request.getQuantity() != null && request.getQuantity() >= 0 ? request.getQuantity() : 1;
        book.setQuantity(qty);
        int avail = request.getAvailableCopies() != null && request.getAvailableCopies() >= 0 ? request.getAvailableCopies() : qty;
        book.setAvailableCopies(avail);

        book.setPrice(request.getPrice());
        book.setEdition(request.getEdition());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setStatus(computeBookStatus(avail));

        LibraryBook saved = bookRepository.save(book);
        return mapBookToResponse(saved);
    }

    @Transactional
    public LibraryBookResponse updateBook(Long rawSchoolId, Long id, LibraryBookRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryBook book = bookRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) book.setTitle(request.getTitle());
        if (request.getBookNumber() != null) book.setBookNumber(request.getBookNumber());
        if (request.getIsbn() != null) book.setIsbn(request.getIsbn());
        if (request.getAuthor() != null && !request.getAuthor().isBlank()) book.setAuthor(request.getAuthor());
        if (request.getPublisher() != null) book.setPublisher(request.getPublisher());
        if (request.getCategory() != null) book.setCategory(request.getCategory());
        if (request.getRackLocation() != null) book.setRackLocation(request.getRackLocation());
        if (request.getPrice() != null) book.setPrice(request.getPrice());
        if (request.getEdition() != null) book.setEdition(request.getEdition());
        if (request.getDescription() != null) book.setDescription(request.getDescription());
        if (request.getCoverImageUrl() != null) book.setCoverImageUrl(request.getCoverImageUrl());

        if (request.getQuantity() != null) {
            book.setQuantity(request.getQuantity());
        }
        if (request.getAvailableCopies() != null) {
            book.setAvailableCopies(request.getAvailableCopies());
        }
        book.setStatus(computeBookStatus(book.getAvailableCopies()));

        LibraryBook saved = bookRepository.save(book);
        return mapBookToResponse(saved);
    }

    @Transactional
    public void deleteBook(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryBook book = bookRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        bookRepository.delete(book);
    }

    // ─── Members ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LibraryMemberResponse> filterMembers(
            Long rawSchoolId,
            String search,
            String memberType,
            String status,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanType = (memberType != null && !memberType.isBlank() && !memberType.equalsIgnoreCase("all")) ? memberType.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<LibraryMember> page = memberRepository.filterMembers(schoolId, cleanSearch, cleanType, cleanStatus, pageable);
        return page.map(this::mapMemberToResponse);
    }

    @Transactional(readOnly = true)
    public LibraryMemberResponse getMemberById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryMember member = memberRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Library member not found with id: " + id));
        return mapMemberToResponse(member);
    }

    @Transactional
    public LibraryMemberResponse createMember(Long rawSchoolId, LibraryMemberRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        LibraryMember member = new LibraryMember();
        member.setSchool(school);
        member.setMemberType(request.getMemberType() != null ? request.getMemberType() : "STUDENT");
        member.setCardNumber(request.getCardNumber());
        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        member.setClassSection(request.getClassSection());
        member.setAdmissionNumber(request.getAdmissionNumber());
        member.setMaxBooksAllowed(request.getMaxBooksAllowed() != null ? request.getMaxBooksAllowed() : 3);
        member.setActiveIssuedCount(0);
        member.setStatus(request.getStatus() != null && !request.getStatus().isBlank() ? request.getStatus() : "Active");

        if (request.getStudentId() != null) {
            Student student = studentRepository.findById(request.getStudentId()).orElse(null);
            member.setStudent(student);
        }
        if (request.getStaffId() != null) {
            Staff staff = staffRepository.findById(request.getStaffId()).orElse(null);
            member.setStaff(staff);
        }

        LibraryMember saved = memberRepository.save(member);
        return mapMemberToResponse(saved);
    }

    @Transactional
    public LibraryMemberResponse updateMember(Long rawSchoolId, Long id, LibraryMemberRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryMember member = memberRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Library member not found with id: " + id));

        if (request.getMemberType() != null) member.setMemberType(request.getMemberType());
        if (request.getCardNumber() != null && !request.getCardNumber().isBlank()) member.setCardNumber(request.getCardNumber());
        if (request.getFullName() != null && !request.getFullName().isBlank()) member.setFullName(request.getFullName());
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getClassSection() != null) member.setClassSection(request.getClassSection());
        if (request.getAdmissionNumber() != null) member.setAdmissionNumber(request.getAdmissionNumber());
        if (request.getMaxBooksAllowed() != null) member.setMaxBooksAllowed(request.getMaxBooksAllowed());
        if (request.getStatus() != null && !request.getStatus().isBlank()) member.setStatus(request.getStatus());

        LibraryMember saved = memberRepository.save(member);
        return mapMemberToResponse(saved);
    }

    @Transactional
    public void deleteMember(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryMember member = memberRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Library member not found with id: " + id));
        memberRepository.delete(member);
    }

    // ─── Circulations (Issue & Return) ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LibraryCirculationResponse> filterCirculations(
            Long rawSchoolId,
            String search,
            String status,
            Long memberId,
            Long bookId,
            Pageable pageable) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;
        String cleanStatus = (status != null && !status.isBlank() && !status.equalsIgnoreCase("all")) ? status.trim() : null;

        Page<LibraryCirculation> page = circulationRepository.filterCirculations(
                schoolId, cleanSearch, cleanStatus, memberId, bookId, pageable);

        // Auto-check and mark overdue in memory if past due date
        LocalDate today = LocalDate.now();
        return page.map(c -> {
            if ("Issued".equalsIgnoreCase(c.getStatus()) && c.getDueDate() != null && c.getDueDate().isBefore(today)) {
                c.setStatus("Overdue");
            }
            return mapCirculationToResponse(c);
        });
    }

    @Transactional(readOnly = true)
    public LibraryCirculationResponse getCirculationById(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        LibraryCirculation circulation = circulationRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Circulation record not found with id: " + id));
        return mapCirculationToResponse(circulation);
    }

    @Transactional
    public LibraryCirculationResponse issueBook(Long rawSchoolId, LibraryIssueRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + schoolId));

        LibraryBook book = bookRepository.findByIdAndSchoolId(request.getBookId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + request.getBookId()));

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No available copies of book '" + book.getTitle() + "' to issue.");
        }

        LibraryMember member = memberRepository.findByIdAndSchoolId(request.getMemberId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + request.getMemberId()));

        if (!"Active".equalsIgnoreCase(member.getStatus())) {
            throw new BadRequestException("Member account is " + member.getStatus() + ". Cannot issue books.");
        }

        if (member.getActiveIssuedCount() >= member.getMaxBooksAllowed()) {
            throw new BadRequestException("Member has reached maximum allowed book limit (" + member.getMaxBooksAllowed() + ").");
        }

        // Create circulation
        LibraryCirculation circ = new LibraryCirculation();
        circ.setSchool(school);
        circ.setBook(book);
        circ.setMember(member);
        circ.setIssueDate(request.getIssueDate() != null ? request.getIssueDate() : LocalDate.now());
        circ.setDueDate(request.getDueDate());
        circ.setStatus("Issued");
        circ.setIssuedBy(request.getIssuedBy());
        circ.setRemarks(request.getRemarks());

        // Update book availability
        int newAvail = book.getAvailableCopies() - 1;
        book.setAvailableCopies(newAvail);
        book.setStatus(computeBookStatus(newAvail));
        bookRepository.save(book);

        // Update member active count
        member.setActiveIssuedCount(member.getActiveIssuedCount() + 1);
        memberRepository.save(member);

        LibraryCirculation saved = circulationRepository.save(circ);
        return mapCirculationToResponse(saved);
    }

    @Transactional
    public LibraryCirculationResponse returnBook(Long rawSchoolId, Long id, LibraryReturnRequest request) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        LibraryCirculation circ = circulationRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Circulation record not found with id: " + id));

        if ("Returned".equalsIgnoreCase(circ.getStatus())) {
            throw new BadRequestException("Book has already been returned.");
        }

        LocalDate returnDate = request != null && request.getReturnDate() != null ? request.getReturnDate() : LocalDate.now();
        circ.setReturnDate(returnDate);
        circ.setStatus("Returned");

        // Calculate overdue fine if applicable: ₹5 per day default or custom
        BigDecimal fine = BigDecimal.ZERO;
        if (request != null && request.getFineAmount() != null) {
            fine = request.getFineAmount();
        } else if (circ.getDueDate() != null && returnDate.isAfter(circ.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(circ.getDueDate(), returnDate);
            fine = BigDecimal.valueOf(daysOverdue * 5.0); // Standard 5 currency units per day
        }
        circ.setFineAmount(fine);
        circ.setFinePaid(request != null && Boolean.TRUE.equals(request.getFinePaid()));
        if (request != null && request.getRemarks() != null) {
            circ.setRemarks(request.getRemarks());
        }

        // Return copy to book inventory
        LibraryBook book = circ.getBook();
        int newAvail = book.getAvailableCopies() + 1;
        book.setAvailableCopies(newAvail);
        book.setStatus(computeBookStatus(newAvail));
        bookRepository.save(book);

        // Decrement member count
        LibraryMember member = circ.getMember();
        member.setActiveIssuedCount(Math.max(0, member.getActiveIssuedCount() - 1));
        memberRepository.save(member);

        LibraryCirculation saved = circulationRepository.save(circ);
        return mapCirculationToResponse(saved);
    }

    @Transactional
    public LibraryCirculationResponse payFine(Long rawSchoolId, Long id) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        LibraryCirculation circ = circulationRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Circulation record not found with id: " + id));

        circ.setFinePaid(true);
        LibraryCirculation saved = circulationRepository.save(circ);
        return mapCirculationToResponse(saved);
    }

    // ─── Stats ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public LibraryStatsResponse getStats(Long rawSchoolId) {
        final Long schoolId = authContextService.resolveSchoolId(rawSchoolId);

        long totalBooks = bookRepository.countBySchoolId(schoolId);
        long totalCopies = bookRepository.sumTotalQuantity(schoolId);
        long availableCopies = bookRepository.sumAvailableCopies(schoolId);
        long issuedBooks = circulationRepository.countBySchoolIdAndStatusIgnoreCase(schoolId, "Issued");
        long overdueBooks = circulationRepository.countOverdue(schoolId, LocalDate.now());
        long totalMembers = memberRepository.countBySchoolId(schoolId);
        BigDecimal finesCollected = circulationRepository.sumFinesCollected(schoolId);
        BigDecimal finesPending = circulationRepository.sumFinesPending(schoolId);

        return LibraryStatsResponse.builder()
                .totalBooks(totalBooks)
                .totalCopies(totalCopies)
                .availableCopies(availableCopies)
                .issuedBooks(issuedBooks)
                .overdueBooks(overdueBooks)
                .totalMembers(totalMembers)
                .finesCollected(finesCollected)
                .finesPending(finesPending)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String computeBookStatus(int availableCopies) {
        if (availableCopies <= 0) return "OUT OF STOCK";
        if (availableCopies <= 2) return "LOW STOCK";
        return "IN STOCK";
    }

    private LibraryBookResponse mapBookToResponse(LibraryBook b) {
        return LibraryBookResponse.builder()
                .id(b.getId())
                .schoolId(b.getSchool().getId())
                .title(b.getTitle())
                .bookNumber(b.getBookNumber())
                .isbn(b.getIsbn())
                .author(b.getAuthor())
                .publisher(b.getPublisher())
                .category(b.getCategory())
                .rackLocation(b.getRackLocation())
                .quantity(b.getQuantity())
                .availableCopies(b.getAvailableCopies())
                .price(b.getPrice())
                .edition(b.getEdition())
                .description(b.getDescription())
                .coverImageUrl(b.getCoverImageUrl())
                .status(b.getStatus())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private LibraryMemberResponse mapMemberToResponse(LibraryMember m) {
        return LibraryMemberResponse.builder()
                .id(m.getId())
                .schoolId(m.getSchool().getId())
                .memberType(m.getMemberType())
                .cardNumber(m.getCardNumber())
                .studentId(m.getStudent() != null ? m.getStudent().getId() : null)
                .staffId(m.getStaff() != null ? m.getStaff().getId() : null)
                .fullName(m.getFullName())
                .email(m.getEmail())
                .phone(m.getPhone())
                .classSection(m.getClassSection())
                .admissionNumber(m.getAdmissionNumber())
                .maxBooksAllowed(m.getMaxBooksAllowed())
                .activeIssuedCount(m.getActiveIssuedCount())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }

    private LibraryCirculationResponse mapCirculationToResponse(LibraryCirculation c) {
        return LibraryCirculationResponse.builder()
                .id(c.getId())
                .schoolId(c.getSchool().getId())
                .bookId(c.getBook().getId())
                .bookTitle(c.getBook().getTitle())
                .bookNumber(c.getBook().getBookNumber())
                .isbn(c.getBook().getIsbn())
                .author(c.getBook().getAuthor())
                .coverImageUrl(c.getBook().getCoverImageUrl())
                .memberId(c.getMember().getId())
                .memberName(c.getMember().getFullName())
                .memberCardNumber(c.getMember().getCardNumber())
                .memberType(c.getMember().getMemberType())
                .classSection(c.getMember().getClassSection())
                .admissionNumber(c.getMember().getAdmissionNumber())
                .issueDate(c.getIssueDate())
                .dueDate(c.getDueDate())
                .returnDate(c.getReturnDate())
                .fineAmount(c.getFineAmount())
                .finePaid(c.getFinePaid())
                .status(c.getStatus())
                .issuedBy(c.getIssuedBy())
                .remarks(c.getRemarks())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
