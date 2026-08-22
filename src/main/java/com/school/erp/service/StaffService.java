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
import com.school.erp.entity.UserRole;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.entity.auth.Role;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.repository.auth.RoleRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import com.school.erp.entity.ClassTeacherAssignment;
import com.school.erp.repository.ClassTeacherAssignmentRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import com.school.erp.service.auth.RoleSyncService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StaffService {

    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final AuthContextService authContextService;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final RoleSyncService roleSyncService;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public StaffService(
            StaffRepository staffRepository,
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            AuthContextService authContextService,
            ClassTeacherAssignmentRepository classTeacherAssignmentRepository,
            UserSchoolRoleRepository userSchoolRoleRepository,
            RoleSyncService roleSyncService,
            RoleRepository roleRepository,
            UserRoleMappingRepository userRoleMappingRepository
    ) {
        this.staffRepository = staffRepository;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.authContextService = authContextService;
        this.classTeacherAssignmentRepository = classTeacherAssignmentRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
        this.roleSyncService = roleSyncService;
        this.roleRepository = roleRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
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

    public com.school.erp.dto.staff.StaffStatsResponse getStaffStats(Long schoolId) {
        Long effectiveSchoolId = authContextService.resolveSchoolId(schoolId);
        List<Staff> staffList = staffRepository.findBySchoolId(effectiveSchoolId);

        long total = staffList.size();
        long present = staffList.stream().filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus())).count();
        long onLeave = staffList.stream().filter(s -> "ON_LEAVE".equalsIgnoreCase(s.getStatus())).count();
        long departments = staffList.stream().map(Staff::getDepartmentId).filter(java.util.Objects::nonNull).distinct().count();

        return new com.school.erp.dto.staff.StaffStatsResponse(total, present, onLeave, departments);
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
        
        Long userIdToUse = request.userId();
        String generatedPassword = null;
        School school = getSchool(effectiveSchoolId);

        if (userIdToUse == null) {
            if (request.phone() == null || request.phone().isBlank()) {
                throw new IllegalArgumentException("Phone number is required to auto-generate login credentials");
            }
            // Find or create User
            User user = userRepository.findByPhone(request.phone()).orElse(null);
            if (user == null) {
                user = new User();
                user.setPhone(request.phone());
                user.setName(request.firstName() + (request.lastName() != null ? " " + request.lastName() : ""));
                user.setEmail(request.email());
                user.setStatus("ACTIVE");
                generatedPassword = UUID.randomUUID().toString().substring(0, 8);
                user.setPasswordHash(PASSWORD_ENCODER.encode(generatedPassword));
                userRepository.save(user);
            }
            userIdToUse = user.getId();
            
            // Assign legacy School Role
            UserRole legacyRoleToAssign = request.designation() != null && request.designation().toLowerCase().contains("teacher") ? UserRole.TEACHER : UserRole.STAFF;
            boolean legacyRoleExists = userSchoolRoleRepository.existsByUserIdAndSchoolIdAndRoleAndStatusIgnoreCase(
                    userIdToUse, effectiveSchoolId, legacyRoleToAssign, "ACTIVE");
            if (!legacyRoleExists) {
                UserSchoolRole usr = new UserSchoolRole();
                usr.setUser(user);
                usr.setSchool(school);
                usr.setRole(legacyRoleToAssign);
                usr.setStatus("ACTIVE");
                userSchoolRoleRepository.save(usr);
            }

            // Assign new RBAC Role explicitly if provided
            if (request.roleId() != null && !request.roleId().isBlank()) {
                Role role = roleRepository.findById(request.roleId()).orElse(null);
                if (role != null) {
                    UserRoleMapping mapping = new UserRoleMapping();
                    mapping.setUser(user);
                    mapping.setSchoolId(effectiveSchoolId);
                    mapping.setRole(role);
                    mapping.setActive(true);
                    userRoleMappingRepository.save(mapping);
                }
            } else {
                // Fallback to auto-syncing if no role explicitly provided
                userSchoolRoleRepository.findByUserIdAndSchoolIdAndStatusIgnoreCase(user.getId(), effectiveSchoolId, "ACTIVE")
                        .ifPresent(roleSyncService::syncUserSchoolRole);
            }
        } else {
            // Prevent duplicate staff profiles for the same user in the same school
            Long finalUserIdToUse = userIdToUse;
            staffRepository.findBySchoolId(effectiveSchoolId).stream()
                    .filter(s -> s.getUserId() != null && s.getUserId().equals(finalUserIdToUse))
                    .findFirst()
                    .ifPresent(s -> {
                        throw new IllegalStateException("User already has a staff profile in this school");
                    });
        }

        Staff staff = new Staff();
        mapRequestToEntity(staff, request, school);
        staff.setUserId(userIdToUse);
        
        Staff savedStaff = staffRepository.save(staff);
        StaffResponse response = toResponse(savedStaff, null);
        
        if (generatedPassword != null) {
            return new StaffResponse(
                    response.id(), response.userId(), response.schoolId(), response.departmentId(),
                    response.designationId(), response.joiningDate(), response.salary(),
                    response.status(), response.firstName(), response.lastName(),
                    response.department(), response.designation(), response.photoUrl(),
                    response.phone(), response.email(), response.assignedClassAndSection(),
                    generatedPassword
            );
        }
        
        return response;
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
                assignedClassAndSection,
                null
        );
    }
}
