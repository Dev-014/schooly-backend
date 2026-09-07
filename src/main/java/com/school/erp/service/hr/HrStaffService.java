package com.school.erp.service.hr;

import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.entity.User;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.dto.auth.UserAssignmentRequest;
import com.school.erp.dto.auth.UserAssignmentResponse;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrStaffService {

    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final UserAssignmentRepository userAssignmentRepository;

    public List<Staff> getStaffBySchool(Long schoolId) {
        return staffRepository.findBySchoolId(schoolId);
    }

    public Staff getStaffById(Long schoolId, Long staffId) {
        return staffRepository.findById(staffId)
                .filter(staff -> staff.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    @Transactional
    public Staff createStaff(Long schoolId, StaffRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = new Staff();
        staff.setSchool(school);
        updateStaffFromRequest(staff, request);
        
        return staffRepository.save(staff);
    }

    @Transactional
    public Staff updateStaff(Long schoolId, Long staffId, StaffRequest request) {
        Staff staff = getStaffById(schoolId, staffId);
        updateStaffFromRequest(staff, request);
        return staffRepository.save(staff);
    }

    private void updateStaffFromRequest(Staff staff, StaffRequest request) {
        staff.setDepartmentId(request.departmentId());
        staff.setDesignationId(request.designationId());
        staff.setJoiningDate(request.joiningDate());
        staff.setSalary(request.salary());
        staff.setStatus(request.status());
        staff.setFirstName(request.firstName());
        staff.setLastName(request.lastName());
        staff.setDepartment(request.department());
        staff.setDesignation(request.designation());
        staff.setPhotoUrl(request.photoUrl());
        staff.setPhone(request.phone());
        staff.setEmail(request.email());
        staff.setBiometricId(request.biometricId());
        staff.setDateOfBirth(request.dateOfBirth());
        staff.setGender(request.gender());
        staff.setMaritalStatus(request.maritalStatus());
        staff.setFatherName(request.fatherName());
        staff.setMotherName(request.motherName());
        staff.setEmergencyContact(request.emergencyContact());
        staff.setCurrentAddress(request.currentAddress());
        staff.setPermanentAddress(request.permanentAddress());
        staff.setQualification(request.qualification());
        staff.setWorkExperience(request.workExperience());
        staff.setContractType(request.contractType());
        staff.setWorkShift(request.workShift());
        staff.setLocation(request.location());
        staff.setNotes(request.notes());
    }

    public List<UserAssignmentResponse> getStaffAssignments(Long schoolId, Long staffId) {
        Staff staff = getStaffById(schoolId, staffId);
        if (staff.getUserId() == null) {
            return List.of();
        }
        return userAssignmentRepository.findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, staff.getUserId()).stream()
                .map(this::toAssignmentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserAssignmentResponse assignRole(Long schoolId, Long staffId, UserAssignmentRequest request) {
        Staff staff = getStaffById(schoolId, staffId);
        if (staff.getUserId() == null) {
            throw new RuntimeException("Staff does not have an associated user account");
        }
        User user = userRepository.findById(staff.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAssignment assignment = new UserAssignment();
        assignment.setSchoolId(schoolId);
        assignment.setUser(user);
        assignment.setAcademicSessionId(request.academicSessionId());
        assignment.setAssignmentType(request.assignmentType());
        assignment.setClassId(request.classId());
        assignment.setSectionId(request.sectionId());
        assignment.setSubjectId(request.subjectId());
        assignment.setDepartmentId(request.departmentId());
        assignment.setEffectiveFrom(LocalDateTime.now());
        assignment.setActive(true);

        return toAssignmentResponse(userAssignmentRepository.save(assignment));
    }

    @Transactional
    public void revokeAssignment(Long schoolId, Long staffId, Long assignmentId) {
        Staff staff = getStaffById(schoolId, staffId);
        if (staff.getUserId() == null) return;
        
        UserAssignment assignment = userAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));
        
        if (!assignment.getSchoolId().equals(schoolId) || !assignment.getUser().getId().equals(staff.getUserId())) {
            throw new RuntimeException("Assignment does not belong to this staff in this school");
        }
        
        assignment.setActive(false);
        assignment.setEffectiveTo(LocalDateTime.now());
        userAssignmentRepository.save(assignment);
    }

    private UserAssignmentResponse toAssignmentResponse(UserAssignment ua) {
        return UserAssignmentResponse.builder()
                .id(ua.getId())
                .schoolId(ua.getSchoolId())
                .userId(ua.getUser().getId())
                .academicSessionId(ua.getAcademicSessionId())
                .assignmentType(ua.getAssignmentType())
                .classId(ua.getClassId())
                .sectionId(ua.getSectionId())
                .subjectId(ua.getSubjectId())
                .departmentId(ua.getDepartmentId())
                .effectiveFrom(ua.getEffectiveFrom())
                .effectiveTo(ua.getEffectiveTo())
                .isActive(ua.isActive())
                .build();
    }
}
