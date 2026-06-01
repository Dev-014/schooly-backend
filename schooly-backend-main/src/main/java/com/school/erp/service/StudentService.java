package com.school.erp.service;

import com.school.erp.dto.student.StudentRequest;
import com.school.erp.dto.student.StudentResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.AcademicYearRepository;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.SectionRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AuthContextService authContextService;

    public StudentService(
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            SchoolClassRepository schoolClassRepository,
            SectionRepository sectionRepository,
            AcademicYearRepository academicYearRepository,
            AuthContextService authContextService
    ) {
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.sectionRepository = sectionRepository;
        this.academicYearRepository = academicYearRepository;
        this.authContextService = authContextService;
    }

    public Page<StudentResponse> getAllStudents(Long schoolId, Long classId, String status, Pageable pageable) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        if (status != null && !status.isBlank()) {
            return (classId == null
                    ? studentRepository.findBySchoolIdAndStatusAndDeletedAtIsNull(effectiveSchoolId, status, pageable)
                    : studentRepository.findBySchoolIdAndSchoolClassIdAndStatusAndDeletedAtIsNull(effectiveSchoolId, classId, status, pageable))
                    .map(this::toResponse);
        }

        return (classId == null
                ? studentRepository.findBySchoolIdAndDeletedAtIsNull(effectiveSchoolId, pageable)
                : studentRepository.findBySchoolIdAndSchoolClassIdAndDeletedAtIsNull(effectiveSchoolId, classId, pageable))
                .map(this::toResponse);
    }

    public Page<StudentResponse> getStudents(Long schoolId, Pageable pageable) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return studentRepository.findBySchoolIdAndDeletedAtIsNull(effectiveSchoolId, pageable)
                .map(this::toResponse);
    }

    public List<StudentResponse> searchStudents(String name, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return studentRepository.findByNameContainingIgnoreCaseAndSchoolIdAndDeletedAtIsNull(name, effectiveSchoolId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StudentResponse getStudentById(Long id, Long schoolId) {
        return toResponse(findStudentByIdAndSchoolId(id, authContextService.resolveSchoolId(schoolId)));
    }

    public StudentResponse getStudentByAdmissionNo(String admissionNo, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        return toResponse(studentRepository.findByAdmissionNoAndSchoolIdAndDeletedAtIsNull(admissionNo, effectiveSchoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found for admissionNo " + admissionNo + " and schoolId " + effectiveSchoolId
                )));
    }

    public StudentResponse createStudent(StudentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        if (studentRepository.existsByAdmissionNoAndSchoolId(request.admissionNo(), effectiveSchoolId)) {
            throw new IllegalArgumentException("Admission number already exists");
        }
        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        Student student = new Student();
        mapRequestToEntity(student, request, school, schoolClass);
        return toResponse(studentRepository.save(student));
    }

    public StudentResponse updateStudent(Long id, Long schoolId, StudentRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        if (studentRepository.existsByAdmissionNoAndSchoolIdAndIdNot(request.admissionNo(), effectiveSchoolId, id)) {
            throw new IllegalArgumentException("Admission number already exists");
        }
        Student student = findStudentByIdAndSchoolId(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        mapRequestToEntity(student, request, school, schoolClass);
        return toResponse(studentRepository.save(student));
    }

    public void deleteStudent(Long id, Long schoolId) {
        Student student = findStudentByIdAndSchoolId(id, authContextService.resolveSchoolId(schoolId));
        student.setStatus("INACTIVE");
        student.setDeletedAt(java.time.LocalDateTime.now());
        studentRepository.save(student);
    }

    private Student findStudentByIdAndSchoolId(Long id, Long schoolId) {
        return studentRepository.findByIdAndSchoolIdAndDeletedAtIsNull(id, schoolId)
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

        if (request.sectionId() != null) {
            sectionRepository.findByIdAndSchoolIdAndDeletedAtIsNull(request.sectionId(), school.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Section not found for id " + request.sectionId() + " and schoolId " + school.getId()
                    ));
            student.setSectionId(request.sectionId());
        } else {
            student.setSectionId(null);
        }

        if (request.academicYearId() != null) {
            academicYearRepository.findByIdAndSchoolIdAndDeletedAtIsNull(request.academicYearId(), school.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Academic year not found for id " + request.academicYearId() + " and schoolId " + school.getId()
                    ));
            student.setAcademicYearId(request.academicYearId());
        } else {
            student.setAcademicYearId(null);
        }

        student.setSchool(school);
        student.setSchoolClass(schoolClass);
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
                student.getAcademicYearId()
        );
    }
}

