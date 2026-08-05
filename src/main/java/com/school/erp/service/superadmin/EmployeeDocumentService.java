package com.school.erp.service.superadmin;

import com.school.erp.dto.EmployeeDocumentDTO;
import com.school.erp.dto.UploadDocumentRequest;
import com.school.erp.entity.SuperAdminEmployee;
import com.school.erp.entity.superadmin.EmployeeDocument;
import com.school.erp.repository.SuperAdminEmployeeRepository;
import com.school.erp.repository.superadmin.EmployeeDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final SuperAdminEmployeeRepository employeeRepository;

    @Transactional(readOnly = true)
    public List<EmployeeDocumentDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeDocumentDTO uploadDocument(UploadDocumentRequest request) {
        SuperAdminEmployee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        SuperAdminEmployee uploader = null;
        if (request.getUploadedBy() != null) {
            uploader = employeeRepository.findById(request.getUploadedBy())
                    .orElseThrow(() -> new RuntimeException("Uploader not found"));
        }

        EmployeeDocument document = new EmployeeDocument();
        document.setEmployee(employee);
        document.setDocumentType(request.getDocumentType());
        document.setFileName(request.getFileName());
        document.setFileUrl(request.getFileUrl());
        document.setUploadedBy(uploader);

        EmployeeDocument savedDocument = documentRepository.save(document);
        return mapToDTO(savedDocument);
    }

    private EmployeeDocumentDTO mapToDTO(EmployeeDocument document) {
        EmployeeDocumentDTO dto = new EmployeeDocumentDTO();
        dto.setId(document.getId());
        dto.setEmployeeId(document.getEmployee().getId());
        dto.setEmployeeName(document.getEmployee().getUser().getName());
        dto.setDocumentType(document.getDocumentType());
        dto.setFileName(document.getFileName());
        dto.setFileUrl(document.getFileUrl());
        dto.setStatus(document.getStatus());
        dto.setUploadedAt(document.getCreatedAt());
        
        if (document.getUploadedBy() != null) {
            dto.setUploadedBy(document.getUploadedBy().getId());
            dto.setUploaderName(document.getUploadedBy().getUser().getName());
        }
        
        return dto;
    }
}
