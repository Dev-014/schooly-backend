package com.school.erp.controller.admin.library;

import com.school.erp.api.ApiResponse;
import com.school.erp.api.PaginationMeta;
import com.school.erp.dto.library.*;
import com.school.erp.security.PermissionRequired;
import com.school.erp.service.library.SchoolLibraryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/admin/library", "/api/v1/library"})
@RequiredArgsConstructor
public class AdminLibraryController {

    private final SchoolLibraryService libraryService;

    // ─── Books ───────────────────────────────────────────────────────────────

    @GetMapping("/books")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<List<LibraryBookResponse>>> getBooks(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<LibraryBookResponse> result = libraryService.filterBooks(
                schoolId, search, category, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Library books retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/books/{id}")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<LibraryBookResponse>> getBookById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.getBookById(schoolId, id),
                "Book details retrieved successfully"));
    }

    @PostMapping("/books")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryBookResponse>> createBook(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody LibraryBookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.createBook(schoolId, request),
                "Book added to catalog successfully"));
    }

    @PutMapping("/books/{id}")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryBookResponse>> updateBook(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody LibraryBookRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.updateBook(schoolId, id, request),
                "Book details updated successfully"));
    }

    @DeleteMapping("/books/{id}")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        libraryService.deleteBook(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Book removed from library"));
    }

    // ─── Members ─────────────────────────────────────────────────────────────

    @GetMapping("/members")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<List<LibraryMemberResponse>>> getMembers(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String memberType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<LibraryMemberResponse> result = libraryService.filterMembers(
                schoolId, search, memberType, status, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Library members retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/members/{id}")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> getMemberById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.getMemberById(schoolId, id),
                "Library member details retrieved successfully"));
    }

    @PostMapping("/members")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> createMember(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody LibraryMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.createMember(schoolId, request),
                "Library member registered successfully"));
    }

    @PutMapping("/members/{id}")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryMemberResponse>> updateMember(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @Valid @RequestBody LibraryMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.updateMember(schoolId, id, request),
                "Library member details updated successfully"));
    }

    @DeleteMapping("/members/{id}")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<Void>> deleteMember(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        libraryService.deleteMember(schoolId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Library member deleted successfully"));
    }

    // ─── Circulation (Issue / Return) ─────────────────────────────────────────

    @GetMapping("/circulations")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<List<LibraryCirculationResponse>>> getCirculations(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<LibraryCirculationResponse> result = libraryService.filterCirculations(
                schoolId, search, status, memberId, bookId, PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success(
                result.getContent(),
                "Library circulation records retrieved successfully",
                new PaginationMeta(result.getNumber(), result.getSize(), result.getTotalElements())));
    }

    @GetMapping("/circulations/{id}")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<LibraryCirculationResponse>> getCirculationById(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.getCirculationById(schoolId, id),
                "Circulation details retrieved successfully"));
    }

    @PostMapping("/circulations/issue")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryCirculationResponse>> issueBook(
            @RequestParam(required = false) Long schoolId,
            @Valid @RequestBody LibraryIssueRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.issueBook(schoolId, request),
                "Book issued successfully"));
    }

    @PostMapping("/circulations/{id}/return")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryCirculationResponse>> returnBook(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id,
            @RequestBody(required = false) LibraryReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.returnBook(schoolId, id, request),
                "Book returned successfully"));
    }

    @PostMapping("/circulations/{id}/pay-fine")
    @PermissionRequired("library.book.edit")
    public ResponseEntity<ApiResponse<LibraryCirculationResponse>> payFine(
            @RequestParam(required = false) Long schoolId,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.payFine(schoolId, id),
                "Overdue fine marked as paid"));
    }

    // ─── Stats ───────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @PermissionRequired("library.book.view")
    public ResponseEntity<ApiResponse<LibraryStatsResponse>> getStats(
            @RequestParam(required = false) Long schoolId) {
        return ResponseEntity.ok(ApiResponse.success(
                libraryService.getStats(schoolId),
                "Library statistics retrieved successfully"));
    }
}
