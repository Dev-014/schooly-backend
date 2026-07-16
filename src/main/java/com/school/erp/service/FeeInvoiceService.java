package com.school.erp.service;

import com.school.erp.dto.feeinvoice.FeeInvoiceRequest;
import com.school.erp.dto.feeinvoice.FeeInvoiceResponse;
import com.school.erp.entity.FeeInvoice;
import com.school.erp.entity.School;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.FeeInvoiceRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeeInvoiceService {

    private final FeeInvoiceRepository feeInvoiceRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public FeeInvoiceService(
            FeeInvoiceRepository feeInvoiceRepository,
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.feeInvoiceRepository = feeInvoiceRepository;
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<FeeInvoiceResponse> getAllInvoices(Long schoolId, Long studentId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<FeeInvoice> invoices = studentId == null
                ? feeInvoiceRepository.findBySchoolId(effectiveSchoolId)
                : feeInvoiceRepository.findBySchoolIdAndStudentId(effectiveSchoolId, studentId);
        return invoices.stream().map(this::toResponse).toList();
    }

    public FeeInvoiceResponse getInvoiceById(Long id, Long schoolId) {
        return toResponse(findInvoice(id, authContextService.resolveSchoolId(schoolId)));
    }

    @Transactional
    public FeeInvoiceResponse createInvoice(FeeInvoiceRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        Student student = getStudent(request.studentId(), effectiveSchoolId);
        FeeInvoice invoice = new FeeInvoice();
        mapRequestToEntity(invoice, request, school, student);
        return toResponse(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public FeeInvoiceResponse updateInvoice(Long id, Long schoolId, FeeInvoiceRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        FeeInvoice invoice = findInvoice(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        Student student = getStudent(request.studentId(), effectiveSchoolId);
        mapRequestToEntity(invoice, request, school, student);
        return toResponse(feeInvoiceRepository.save(invoice));
    }

    @Transactional
    public void deleteInvoice(Long id, Long schoolId) {
        FeeInvoice invoice = findInvoice(id, authContextService.resolveSchoolId(schoolId));
        feeInvoiceRepository.delete(invoice);
    }

    private FeeInvoice findInvoice(Long id, Long schoolId) {
        return feeInvoiceRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private Student getStudent(Long studentId, Long schoolId) {
        return studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found for id " + studentId + " and schoolId " + schoolId
                ));
    }

    private void mapRequestToEntity(FeeInvoice invoice, FeeInvoiceRequest request, School school, Student student) {
        invoice.setSchool(school);
        invoice.setStudent(student);
        invoice.setAcademicYearId(request.academicYearId());
        invoice.setDueDate(request.dueDate());
        invoice.setTotalAmount(request.totalAmount());
        invoice.setPaidAmount(request.paidAmount());
        invoice.setStatus(request.status());
    }

    private FeeInvoiceResponse toResponse(FeeInvoice invoice) {
        return new FeeInvoiceResponse(
                invoice.getId(),
                invoice.getStudent().getId(),
                invoice.getSchool().getId(),
                invoice.getAcademicYearId(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getStatus(),
                invoice.getCreatedAt()
        );
    }
}
