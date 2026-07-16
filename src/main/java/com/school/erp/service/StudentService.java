package com.school.erp.service;

import com.school.erp.dto.student.StudentRequest;
import com.school.erp.dto.student.StudentResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.SchoolClass;
import com.school.erp.entity.Student;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolClassRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StudentRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AuthContextService authContextService;

    public StudentService(
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            SchoolClassRepository schoolClassRepository,
            AuthContextService authContextService
    ) {
        this.studentRepository = studentRepository;
        this.schoolRepository = schoolRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.authContextService = authContextService;
    }

    public List<StudentResponse> getAllStudents(Long schoolId, Long classId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Student> students = classId == null
                ? studentRepository.findBySchoolId(effectiveSchoolId)
                : studentRepository.findBySchoolIdAndSchoolClassId(effectiveSchoolId, classId);
        return students.stream().map(this::toResponse).toList();
    }

    public StudentResponse getStudentById(Long id, Long schoolId) {
        return toResponse(findStudentByIdAndSchoolId(id, authContextService.resolveSchoolId(schoolId)));
    }

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        Student student = new Student();
        mapRequestToEntity(student, request, school, schoolClass);
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse updateStudent(Long id, Long schoolId, StudentRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        Student student = findStudentByIdAndSchoolId(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        SchoolClass schoolClass = getClass(request.classId(), effectiveSchoolId);
        mapRequestToEntity(student, request, school, schoolClass);
        return toResponse(studentRepository.save(student));
    }

    @Transactional
    public void deleteStudent(Long id, Long schoolId) {
        Student student = findStudentByIdAndSchoolId(id, authContextService.resolveSchoolId(schoolId));
        studentRepository.delete(student);
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
