package com.school.erp.controller.superadmin;

import com.school.erp.dto.EmployeeDocumentDTO;
import com.school.erp.dto.UploadDocumentRequest;
import com.school.erp.service.superadmin.EmployeeDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/super-admin/documents")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    @GetMapping
    public ResponseEntity<List<EmployeeDocumentDTO>> getAllDocuments() {
        return ResponseEntity.ok(documentService.getAllDocuments());
    }

    @PostMapping("/upload")
    public ResponseEntity<EmployeeDocumentDTO> uploadDocument(@Valid @RequestBody UploadDocumentRequest request) {
        return ResponseEntity.ok(documentService.uploadDocument(request));
    }
}
