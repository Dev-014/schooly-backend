package com.school.erp.service;

import com.school.erp.dto.staff.StaffRequest;
import com.school.erp.dto.staff.StaffResponse;
import com.school.erp.entity.School;
import com.school.erp.entity.Staff;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.StaffRepository;
import com.school.erp.security.AuthContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;
    private final AuthContextService authContextService;

    public StaffService(
            StaffRepository staffRepository,
            SchoolRepository schoolRepository,
            AuthContextService authContextService
    ) {
        this.staffRepository = staffRepository;
        this.schoolRepository = schoolRepository;
        this.authContextService = authContextService;
    }

    public List<StaffResponse> getAllStaff(Long schoolId) {
        return staffRepository.findBySchoolId(authContextService.resolveSchoolId(schoolId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StaffResponse getStaffById(Long id, Long schoolId) {
        return toResponse(findStaff(id, authContextService.resolveSchoolId(schoolId)));
    }

    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        Staff staff = new Staff();
        School school = getSchool(authContextService.resolveSchoolId(request.schoolId()));
        mapRequestToEntity(staff, request, school);
        return toResponse(staffRepository.save(staff));
    }

    @Transactional
    public StaffResponse updateStaff(Long id, Long schoolId, StaffRequest request) {
        authContextService.validateSameSchool(schoolId, request.schoolId());
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId != null ? schoolId : request.schoolId());
        Staff staff = findStaff(id, effectiveSchoolId);
        School school = getSchool(effectiveSchoolId);
        mapRequestToEntity(staff, request, school);
        return toResponse(staffRepository.save(staff));
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
    }

    private StaffResponse toResponse(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getUserId(),
                staff.getSchool().getId(),
                staff.getDepartmentId(),
                staff.getDesignationId(),
                staff.getJoiningDate(),
                staff.getSalary(),
                staff.getStatus()
        );
    }
}
