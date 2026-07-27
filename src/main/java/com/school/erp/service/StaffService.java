package com.school.erp.service;

import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.dto.staff.StaffResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.security.AuthContextService;
import com.school.erp.entity.User;
import com.school.erp.entity.ClassTeacherAssignment;
import com.school.erp.repository.ClassTeacherAssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;

    public StaffService(
            StaffRepository staffRepository,
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            AuthContextService authContextService,
            ClassTeacherAssignmentRepository classTeacherAssignmentRepository
    ) {
        this.staffRepository = staffRepository;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
    }

    public List<StaffResponse> getAllStaff(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Staff> staffList = staffRepository.findBySchoolId(effectiveSchoolId);
        
        Map<Long, String> assignmentMap = classTeacherAssignmentRepository.findBySchoolIdAndStatus(effectiveSchoolId, "ACTIVE").stream()
                .collect(Collectors.toMap(
                        a -> a.getStaff().getId(),
                        a -> {
                            String name = a.getSchoolClass().getName();
                            if (a.getSection() != null) {
                                name += " - " + a.getSection().getName();
                            }
                            return name;
                        },
                        (v1, v2) -> v1
                ));

        return staffList.stream()
                .map(staff -> toResponse(staff, assignmentMap.get(staff.getId())))
                .toList();
    }

    public StaffResponse getStaffById(Long id, Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        Staff staff = findStaff(id, effectiveSchoolId);
        String assignment = classTeacherAssignmentRepository.findBySchoolIdAndStatus(effectiveSchoolId, "ACTIVE").stream()
                .filter(a -> a.getStaff().getId().equals(staff.getId()))
                .map(a -> {
                    String name = a.getSchoolClass().getName();
                    if (a.getSection() != null) {
                        name += " - " + a.getSection().getName();
                    }
                    return name;
                })
                .findFirst()
                .orElse(null);
        return toResponse(staff, assignment);
    }

    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(request.schoolId());
        
        // Prevent duplicate staff profiles for the same user in the same school
        staffRepository.findBySchoolId(effectiveSchoolId).stream()
                .filter(s -> s.getUserId() != null && s.getUserId().equals(request.userId()))
                .findFirst()
                .ifPresent(s -> {
                    throw new IllegalStateException("User already has a staff profile in this school");
                });

        Staff staff = new Staff();
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(staff, request, school);
        return toResponse(staffRepository.save(staff), null);
    }

    @Transactional
    public StaffResponse updateStaff(Long id, Long schoolId, StaffRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        Staff staff = findStaff(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(staff, request, school);
        
        String assignment = classTeacherAssignmentRepository.findBySchoolIdAndStatus(effectiveSchoolId, "ACTIVE").stream()
                .filter(a -> a.getStaff().getId().equals(staff.getId()))
                .map(a -> {
                    String name = a.getSchoolClass().getName();
                    if (a.getSection() != null) {
                        name += " - " + a.getSection().getName();
                    }
                    return name;
                })
                .findFirst()
                .orElse(null);
                
        return toResponse(staffRepository.save(staff), assignment);
    }

    @Transactional
    public void deleteStaff(Long id, Long schoolId) {
        Staff staff = findStaff(id, authContextService.resolveSchoolId(schoolId));
        staffRepository.delete(staff);
    }

    private Staff findStaff(Long id, Long schoolId) {
        return staffRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff not found for id " + id + " and schoolId " + schoolId
                ));
    }

    private School getSchool(Long schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + schoolId));
    }

    private void mapRequestToEntity(Staff staff, StaffRequest request, School school) {
        staff.setUserId(request.userId());
        staff.setDepartmentId(request.departmentId());
        staff.setDesignationId(request.designationId());
        staff.setJoiningDate(request.joiningDate());
        staff.setSalary(request.salary());
        staff.setStatus(request.status());
        staff.setSchool(school);
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setDepartment(request.department());
        staff.setDesignation(request.designation());
        staff.setPhotoUrl(request.photoUrl());
        staff.setPhone(request.phone());
        staff.setEmail(request.email());
    }

    private StaffResponse toResponse(Staff staff) {
        return toResponse(staff, null);
    }

    private StaffResponse toResponse(Staff staff, String assignedClassAndSection) {
        String firstName = staff.getFirstName();
        String lastName = staff.getLastName();
        String email = staff.getEmail();
        String phone = staff.getPhone();

        if (staff.getUserId() != null && (firstName == null || lastName == null || email == null)) {
            User user = userRepository.findById(staff.getUserId()).orElse(null);
            if (user != null) {
                if (firstName == null || lastName == null) {
                    String[] parts = user.getName() != null ? user.getName().split(" ", 2) : new String[]{"Unknown", ""};
                    if (firstName == null) firstName = parts[0];
                    if (lastName == null) lastName = parts.length > 1 ? parts[1] : "";
                }
                if (email == null) email = user.getEmail();
                if (phone == null) phone = user.getPhone();
            }
        }

        return new StaffResponse(
                staff.getId(),
                staff.getUserId(),
                staff.getSchool().getId(),
                staff.getDepartmentId(),
                staff.getDesignationId(),
                staff.getJoiningDate(),
                staff.getSalary(),
                staff.getStatus(),
                firstName,
                lastName,
                staff.getDepartment(),
                staff.getDesignation(),
                staff.getPhotoUrl(),
                phone,
                email,
                assignedClassAndSection
        );
    }
}
