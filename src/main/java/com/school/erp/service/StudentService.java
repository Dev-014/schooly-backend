package com.school.erp.service;

import com.school.erp.dto.catalog.EntitlementEvaluationDto;
import com.school.erp.dto.student.*;
import com.school.erp.entity.*;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.*;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentDocumentRepository documentRepository;
    private final AuthContextService authContextService;
    private final EntitlementService entitlementService;
    private final AcademicYearRepository academicYearRepository;
    private final StudentCategoryRepository studentCategoryRepository;
    private final StudentHouseRepository studentHouseRepository;

    public StudentService(
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            SchoolClassRepository schoolClassRepository,
            StudentDocumentRepository documentRepository,
            AuthContextService authContextService,
            EntitlementService entitlementService,
            AcademicYearRepository academicYearRepository,
            StudentCategoryRepository studentCategoryRepository,
            StudentHouseRepository studentHouseRepository
    ) {
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.documentRepository = documentRepository;
        this.authContextService = authContextService;
        this.entitlementService = entitlementService;
        this.academicYearRepository = academicYearRepository;
        this.studentCategoryRepository = studentCategoryRepository;
        this.studentHouseRepository = studentHouseRepository;
    }

    public List<StudentResponse> getAllStudents(Long schoolId, Long classId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        List<Student> students = classId == null
                ? studentRepository.findBySchoolId(effectiveSchoolId)
                : studentRepository.findBySchoolIdAndSchoolClassId(effectiveSchoolId, classId);
        return students.stream().map(this::toResponse).toList();
    }

    public StudentResponse getStudentById(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        return toResponse(findStudentByIdAndSchoolId(id, effectiveSchoolId));
    }

    public StudentStatsResponse getStudentStats(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        List<Student> all = studentRepository.findBySchoolId(effectiveSchoolId);
        long total = all.size();
        long active = all.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count();
        long inactive = total - active;
        int maxAllowed = entitlementService.getMaxAllowedStudents(effectiveSchoolId);
        EntitlementEvaluationDto eval = entitlementService.evaluateEntitlements(effectiveSchoolId);
        return new StudentStatsResponse(total, active, inactive, maxAllowed, eval.getActivePlanCode(), eval.getActivePlanName());
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceStudentQuota(effectiveSchoolId);

        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        Student student = new Student();
        mapRequestToEntity(student, request, school, schoolClass);

        if (student.getAcademicYearId() == null) {
            Long activeYearId = academicYearRepository.findBySchoolId(effectiveSchoolId).stream()
                    .filter(y -> "ACTIVE".equalsIgnoreCase(y.getStatus()))
                    .findFirst()
                    .map(AcademicYear::getId)
                    .orElse(null);
            student.setAcademicYearId(activeYearId);
        }

        if (student.getName() == null || student.getName().isBlank()) {
            String combined = ((request.firstName() != null ? request.firstName() : "") + " " + (request.lastName() != null ? request.lastName() : "")).trim();
            student.setName(combined.isEmpty() ? "Student " + request.admissionNo() : combined);
        }

        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudent(Long id, Long schoolId, StudentRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Student student = findStudentByIdAndSchoolId(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        mapRequestToEntity(student, request, school, schoolClass);
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Student student = findStudentByIdAndSchoolId(id, effectiveSchoolId);
        studentRepository.delete(student);
    }

    @Transactional
    public int promoteStudents(Long schoolId, StudentPromotionRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        SchoolClass targetClass = getClass(request.targetClassId(), effectiveSchoolId);
        int promotedCount = 0;
        for (Long studentId : request.studentIds()) {
            Student student = studentRepository.findByIdAndSchoolId(studentId, effectiveSchoolId).orElse(null);
            if (student != null) {
                student.setSchoolClass(targetClass);
                if (request.targetSectionId() != null) {
                    student.setSectionId(request.targetSectionId());
                }
                if (request.targetAcademicYearId() != null) {
                    student.setAcademicYearId(request.targetAcademicYearId());
                }
                studentRepository.save(student);
                promotedCount++;
            }
        }
        return promotedCount;
    }

    @Transactional
    public List<StudentResponse> bulkImportStudents(Long schoolId, List<StudentRequest> requests) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        long currentCount = studentRepository.countBySchoolId(effectiveSchoolId);
        int maxAllowed = entitlementService.getMaxAllowedStudents(effectiveSchoolId);
        if (maxAllowed > 0 && currentCount + requests.size() > maxAllowed) {
            throw new BadRequestException(
                    "Bulk import (" + requests.size() + " students) exceeds student quota limit (" + currentCount + "/" + maxAllowed + ") for your active subscription tier."
            );
        }

        School school = getSchool(effectiveSchoolId);
        List<StudentResponse> responses = new ArrayList<>();
        for (StudentRequest req : requests) {
            SchoolClass schoolClass = getClass(req.classId(), effectiveSchoolId);
            Student student = new Student();
            mapRequestToEntity(student, req, school, schoolClass);
            Student saved = studentRepository.save(student);
            responses.add(toResponse(saved));
        }
        return responses;
    }

    public List<StudentDocumentResponse> getStudentDocuments(Long studentId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        findStudentByIdAndSchoolId(studentId, effectiveSchoolId);
        return documentRepository.findByStudentIdAndSchoolId(studentId, effectiveSchoolId).stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    public List<StudentResponse> searchStudents(String query, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        String searchQuery = query.toLowerCase();
        return studentRepository.findBySchoolId(effectiveSchoolId).stream()
                .filter(s -> {
                    String name = (s.getName() != null ? s.getName() : "") + 
                                 (s.getFirstName() != null ? s.getFirstName() : "") + 
                                 (s.getLastName() != null ? s.getLastName() : "");
                    String admissionNo = s.getAdmissionNo() != null ? s.getAdmissionNo() : "";
                    return name.toLowerCase().contains(searchQuery) || 
                           admissionNo.toLowerCase().contains(searchQuery);
                })
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudentDocumentResponse addStudentDocument(Long studentId, Long schoolId, StudentDocumentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        Student student = findStudentByIdAndSchoolId(studentId, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);

        StudentDocument doc = new StudentDocument();
        doc.setStudent(student);
        doc.setSchool(school);
        doc.setDocumentName(request.documentName());
        doc.setDocumentType(request.documentType() != null ? request.documentType() : "GENERAL");
        doc.setFileUrl(request.fileUrl());
        return toDocumentResponse(documentRepository.save(doc));
    }

    @Transactional
    public void deleteStudentDocument(Long documentId, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        entitlementService.enforceModuleAccess(effectiveSchoolId, "STUDENT_INFO");
        entitlementService.enforceSchoolActive(effectiveSchoolId);

        StudentDocument doc = documentRepository.findByIdAndSchoolId(documentId, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        documentRepository.delete(doc);
    }

    private Student findStudentByIdAndSchoolId(Long id, Long schoolId) {
        return studentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private SchoolClass getClass(Long classId, Long schoolId) {
        return schoolClassRepository.findByIdAndSchoolId(classId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Class not found for id " + classId + " and schoolId " + schoolId
                ));
    }

    private void mapRequestToEntity(Student student, StudentRequest request, School school, SchoolClass schoolClass) {
        student.setUserId(request.userId());
        student.setName(request.name());
        student.setAdmissionNo(request.admissionNo());
        student.setRollNumber(request.rollNumber());
        student.setStatus(request.status());
        student.setAdmissionDate(request.admissionDate());
        student.setSectionId(request.sectionId());
        student.setAcademicYearId(request.academicYearId());
        student.setSchool(school);
        student.setSchoolClass(schoolClass);
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setGender(request.gender());
        student.setDateOfBirth(request.dateOfBirth());
        student.setBloodGroup(request.bloodGroup());
        student.setReligion(request.religion());
        student.setNationality(request.nationality());
        student.setPreviousSchool(request.previousSchool());
        student.setAddress(request.address());
        student.setPhotoUrl(request.photoUrl());
        student.setGuardianName(request.guardianName());
        student.setGuardianRelation(request.guardianRelation());
        student.setGuardianPhone(request.guardianPhone());
        student.setGuardianEmail(request.guardianEmail());
        student.setGuardianOccupation(request.guardianOccupation());

        if (request.categoryId() != null) {
            student.setCategory(studentCategoryRepository.findById(request.categoryId()).orElse(null));
        } else {
            student.setCategory(null);
        }

        if (request.houseId() != null) {
            student.setHouse(studentHouseRepository.findById(request.houseId()).orElse(null));
        } else {
            student.setHouse(null);
        }
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getUserId(),
                student.getName(),
                student.getAdmissionNo(),
                student.getRollNumber(),
                student.getStatus(),
                student.getAdmissionDate(),
                student.getSchool().getId(),
                student.getSchoolClass().getId(),
                student.getSectionId(),
                student.getAcademicYearId(),
                student.getFirstName(),
                student.getLastName(),
                student.getGender(),
                student.getDateOfBirth(),
                student.getBloodGroup(),
                student.getReligion(),
                student.getNationality(),
                student.getPreviousSchool(),
                student.getAddress(),
                student.getPhotoUrl(),
                student.getGuardianName(),
                student.getGuardianRelation(),
                student.getGuardianPhone(),
                student.getGuardianEmail(),
                student.getGuardianOccupation(),
                student.getCategory() != null ? student.getCategory().getId() : null,
                student.getHouse() != null ? student.getHouse().getId() : null
        );
    }

    private StudentDocumentResponse toDocumentResponse(StudentDocument doc) {
        return new StudentDocumentResponse(
                doc.getId(),
                doc.getStudent().getId(),
                doc.getSchool().getId(),
                doc.getDocumentName(),
                doc.getDocumentType(),
                doc.getFileUrl(),
                doc.getUploadedAt()
        );
    }
}
