package com.school.erp.service;

import com.school.erp.dto.auth.*;
import com.school.erp.entity.*;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.*;
import com.school.erp.security.JwtUtil;
import com.school.erp.service.auth.AuthorizationService;
import com.school.erp.service.auth.RoleSyncService;
import com.school.erp.entity.auth.RoleArchetype;
import com.school.erp.entity.auth.UserRoleMapping;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import com.school.erp.dto.auth.PersonaDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final AuthSessionRepository authSessionRepository;
    private final StudentParentRepository studentParentRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final JwtUtil jwtUtil;

    private final AuthorizationService authorizationService;
    private final RoleSyncService roleSyncService;
    private final com.school.erp.repository.auth.RolePermissionRepository rolePermissionRepository;

    public AuthService(
            UserRepository userRepository,
            UserRoleMappingRepository userRoleMappingRepository,
            AuthSessionRepository authSessionRepository,
            StudentParentRepository studentParentRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            JwtUtil jwtUtil,
            AuthorizationService authorizationService,
            RoleSyncService roleSyncService,
            com.school.erp.repository.auth.RolePermissionRepository rolePermissionRepository
    ) {
        this.userRepository = userRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
        this.authSessionRepository = authSessionRepository;
        this.studentParentRepository = studentParentRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.jwtUtil = jwtUtil;
        this.authorizationService = authorizationService;
        this.roleSyncService = roleSyncService;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    // ──────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────
    @Transactional
    public AuthUserResponse loginOrSignup(String phone) {
        return userRepository.findByPhone(phone)
                .map(user -> toUserResponse(user, false))
                .orElseGet(() -> toUserResponse(userRepository.save(newUser(phone)), true));
    }

    @Transactional
    public AuthTokenResponse loginStudentWithCredentials(StudentLoginRequest request, String deviceInfo) {
        // Find Student by admissionNo
        Student student = studentRepository.findByAdmissionNo(request.admissionNo())
                .orElseThrow(() -> new UnauthorizedException("Student not found for admission number: " + request.admissionNo()));
        
        if (student.getUserId() == null) {
            throw new UnauthorizedException("Student account is not fully set up.");
        }

        User user = userRepository.findById(student.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        // Verify Password
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        if (user.getPasswordHash() == null || !encoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid password");
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new UnauthorizedException("User account is not active");
        }

        Long schoolId = student.getSchool().getId();
        UserRole role = UserRole.STUDENT;

        String accessToken = jwtUtil.generateAccessToken(user.getId(), schoolId, role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), schoolId, role);

        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setSchool(student.getSchool());
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        authSessionRepository.save(session);

        List<PermissionContextDto> permissions = rolePermissionRepository.findBySchoolIdAndRoleId(null, "role_student_global")
                .stream()
                .map(rp -> new PermissionContextDto(rp.getPermission().getPermissionKey(), rp.getScopeType()))
                .toList();

        PersonaDto persona = new PersonaDto("STUDENT", "STUDENT", RoleArchetype.STUDENT);

        return new AuthTokenResponse(
                user.getId(),
                schoolId,
                student.getId(),   // Pass studentId so frontend can use it directly
                user.getName() != null ? user.getName() : "",
                "STUDENT",
                accessToken,
                refreshToken,
                permissions,
                List.of(persona),
                persona
        );
    }

    @Transactional
    public LoginVerifyResponse verifyOtp(OtpVerifyRequest request, String deviceInfo) {
        if (!"1111".equals(request.otp())) {
            throw new UnauthorizedException("Invalid 4-digit verification code");
        }

        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new UnauthorizedException("User not found for phone " + request.phone()));

        List<UserRoleMapping> userRoles = userRoleMappingRepository.findByUserIdAndIsActiveTrue(user.getId());
        List<String> roleNames = new ArrayList<>(userRoles.stream().map(r -> extractRoleEnum(r).name()).distinct().toList());
        if (roleNames.isEmpty()) {
            boolean isParent = !studentParentRepository.findByIdParentUserId(user.getId()).isEmpty();
            if (isParent) {
                roleNames.add("PARENT");
            } else {
                roleNames.add("STUDENT"); // Fallback for basic users
            }
        }

        String primaryRole = determinePrimaryRole(roleNames);
        List<UserSchoolResponse> schools = userRoles.stream()
                .filter(r -> r.getSchoolId() != null)
                .map(this::toSchoolResponse)
                .distinct()
                .toList();

        boolean requiresSchoolSelection = schools.size() > 1 && !roleNames.contains("SUPER_ADMIN");

        List<StudentSummaryDto> students = new ArrayList<>();
        boolean requiresStudentSelection = false;

        if (roleNames.contains("PARENT")) {
            List<StudentParent> parents = studentParentRepository.findByIdParentUserId(user.getId());
            for (StudentParent sp : parents) {
                Student s = sp.getStudent();
                if (s != null) {
                    String className = s.getSchoolClass() != null ? s.getSchoolClass().getName() : "General";
                    Long schoolId = s.getSchool() != null ? s.getSchool().getId() : null;
                    String schoolName = s.getSchool() != null ? s.getSchool().getName() : "Greenwood Academy";
                    students.add(new StudentSummaryDto(s.getId(), s.getName(), s.getAdmissionNo(), className, schoolId, schoolName));
                }
            }
            if (students.size() > 1) {
                requiresStudentSelection = true;
            }
        } else if (roleNames.contains("STUDENT")) {
            List<Student> studentRecords = studentRepository.findByUserId(user.getId());
            for (Student s : studentRecords) {
                String className = s.getSchoolClass() != null ? s.getSchoolClass().getName() : "General";
                Long schoolId = s.getSchool() != null ? s.getSchool().getId() : null;
                String schoolName = s.getSchool() != null ? s.getSchool().getName() : "Greenwood Academy";
                students.add(new StudentSummaryDto(s.getId(), s.getName(), s.getAdmissionNo(), className, schoolId, schoolName));
            }
            if (students.size() > 1) {
                requiresStudentSelection = true;
            }
        }

        String accessToken = null;
        String refreshToken = null;
        Long targetSchoolId = null;

        if (!requiresSchoolSelection) {
            targetSchoolId = !schools.isEmpty() ? schools.get(0).schoolId() : null;
            UserRole targetRole = userRoles.isEmpty() ? UserRole.STUDENT : extractRoleEnum(userRoles.get(0));
            for (UserRoleMapping usr : userRoles) {
                if (extractRoleEnum(usr).name().equals(primaryRole)) {
                    targetRole = extractRoleEnum(usr);
                    targetSchoolId = usr.getSchoolId();
                    break;
                }
            }
            accessToken = jwtUtil.generateAccessToken(user.getId(), targetSchoolId, targetRole);
            refreshToken = jwtUtil.generateRefreshToken(user.getId(), targetSchoolId, targetRole);

            AuthSession authSession = new AuthSession();
            authSession.setUser(user);
            if (!schools.isEmpty() && targetSchoolId != null) {
                School school = schoolRepository.findById(targetSchoolId).orElse(null);
                if (school != null) {
                    if ("SUSPENDED".equalsIgnoreCase(school.getStatus())) {
                        throw new UnauthorizedException("SCHOOL_SUSPENDED");
                    }
                    authSession.setSchool(school);
                }
            }
            authSession.setAccessToken(accessToken);
            authSession.setRefreshToken(refreshToken);
            authSessionRepository.save(authSession);
        }

        List<PermissionContextDto> permissions;
        if ("SUPER_ADMIN".equalsIgnoreCase(primaryRole) || "SUPERADMIN".equalsIgnoreCase(primaryRole) || "ADMIN".equalsIgnoreCase(primaryRole)) {
            // Temporary fallback: Treat primary ADMIN as ALL until full backend role management is deployed
            permissions = List.of(new PermissionContextDto("ALL", "GLOBAL"));
        } else if (targetSchoolId != null) {
            permissions = authorizationService.getEffectivePermissions(targetSchoolId, user.getId())
                    .stream()
                    .map(rp -> new PermissionContextDto(rp.getPermission().getPermissionKey(), rp.getScopeType()))
                    .toList();
        } else {
            permissions = List.of();
        }

        return new LoginVerifyResponse(
                user.getId(),
                user.getPhone(),
                user.getName(),
                user.getEmail(),
                primaryRole,
                roleNames,
                schools,
                requiresSchoolSelection,
                requiresStudentSelection,
                students,
                accessToken,
                refreshToken,
                permissions,
                List.of(new PersonaDto(primaryRole, primaryRole, RoleArchetype.STAFF)),
                new PersonaDto(primaryRole, primaryRole, RoleArchetype.STAFF)
        );
    }

    private String determinePrimaryRole(List<String> roles) {
        if (roles.contains("SUPER_ADMIN")) return "SUPER_ADMIN";
        if (roles.contains("ADMIN")) return "ADMIN";
        if (roles.contains("TEACHER")) return "TEACHER";
        if (roles.contains("PARENT")) return "PARENT";
        if (roles.contains("STUDENT")) return "STUDENT";
        return roles.isEmpty() ? "STUDENT" : roles.get(0);
    }

    // ──────────────────────────────────────────────
    // SCHOOLS & SELECT
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserSchoolResponse> getUserSchools(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found for id " + userId);
        }

        return userRoleMappingRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .filter(r -> r.getSchoolId() != null)
                .map(this::toSchoolResponse)
                .toList();
    }

    @Transactional
    public AuthTokenResponse selectSchool(Long userId, Long schoolId, String deviceInfo) {
        UserRoleMapping userSchoolRole = userRoleMappingRepository
                .findBySchoolIdAndUserIdAndIsActiveTrue(schoolId, userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active school membership not found for userId " + userId + " and schoolId " + schoolId
                ));

        School school = schoolRepository.findById(userSchoolRole.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + userSchoolRole.getSchoolId()));

        if ("SUSPENDED".equalsIgnoreCase(school.getStatus())) {
            throw new UnauthorizedException("SCHOOL_SUSPENDED");
        }

        String accessToken = jwtUtil.generateAccessToken(userId, schoolId, extractRoleEnum(userSchoolRole));
        String refreshToken = jwtUtil.generateRefreshToken(userId, schoolId, extractRoleEnum(userSchoolRole));

        AuthSession authSession = new AuthSession();
        authSession.setUser(userSchoolRole.getUser());
        authSession.setSchool(school);
        authSession.setAccessToken(accessToken);
        authSession.setRefreshToken(refreshToken);
        authSession.setDeviceInfo(deviceInfo != null ? deviceInfo : null);
        authSessionRepository.save(authSession);

        List<PermissionContextDto> permissions;
        if ("SUPER_ADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name()) || "SUPERADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name()) || "ADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name())) {
            permissions = List.of(new PermissionContextDto("ALL", "GLOBAL"));
        } else {
            permissions = authorizationService.getEffectivePermissions(schoolId, userId)
                    .stream()
                    .map(rp -> new PermissionContextDto(rp.getPermission().getPermissionKey(), rp.getScopeType()))
                    .toList();
        }

        return new AuthTokenResponse(
                userId,
                schoolId,
                null,   // not a student login
                userSchoolRole.getUser().getName() != null ? userSchoolRole.getUser().getName() : "",
                extractRoleEnum(userSchoolRole).name(),
                accessToken,
                refreshToken,
                permissions,
                List.of(new PersonaDto(extractRoleEnum(userSchoolRole).name(), extractRoleEnum(userSchoolRole).name(), RoleArchetype.STAFF)),
                new PersonaDto(extractRoleEnum(userSchoolRole).name(), extractRoleEnum(userSchoolRole).name(), RoleArchetype.STAFF)
        );
    }
    // ──────────────────────────────────────────────
    // REGISTER USER TO SCHOOL
    // ──────────────────────────────────────────────

    @Transactional
    public AuthUserResponse registerUserToSchool(RegisterUserRequest request) {
        UserRole role = parseRole(request.role());

        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + request.schoolId()));

        // Find or create user
        User user = userRepository.findByPhone(request.phone())
                .orElseGet(() -> {
                    User newUser = newUser(request.phone());
                    newUser.setName(request.name());
                    newUser.setEmail(request.email());
                    if (request.password() != null && !request.password().isBlank()) {
                        newUser.setPasswordHash(request.password()); // TODO: hash with BCrypt
                    }
                    return userRepository.save(newUser);
                });

        // Check if role assignment already exists
        String roleId = "role_teacher_global";
        if (role == UserRole.ADMIN) {
            roleId = "role_school_admin_global";
        }
        final String finalRoleId = roleId;

        if (userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(school.getId(), user.getId())
                .stream().anyMatch(m -> finalRoleId.equals(m.getRole().getId()))) {
            throw new BadRequestException(
                    "User already has role " + role.name() + " in school " + school.getName()
            );
        }

        // Assign role
        com.school.erp.entity.auth.Role roleEntity = new com.school.erp.entity.auth.Role();
        roleEntity.setId(finalRoleId);
        
        UserRoleMapping newMapping = new UserRoleMapping();
        newMapping.setSchoolId(school.getId());
        newMapping.setUser(user);
        newMapping.setRole(roleEntity);
        newMapping.setActive(true);
        userRoleMappingRepository.save(newMapping);

        return toUserResponse(user, false);
    }

    // ──────────────────────────────────────────────
    // REGISTER STUDENT (User + Role + Student Record)
    // ──────────────────────────────────────────────

    @Transactional
    public AuthUserResponse registerStudent(RegisterStudentRequest request) {
        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found for id " + request.schoolId()));

        // Find or create user
        User user = userRepository.findByPhone(request.phone())
                .orElseGet(() -> {
                    User newUser = newUser(request.phone());
                    newUser.setName(request.name());
                    newUser.setEmail(request.email());
                    return userRepository.save(newUser);
                });

        // Assign STUDENT role if not already assigned
        if (userRoleMappingRepository.findBySchoolIdAndUserIdAndIsActiveTrue(school.getId(), user.getId())
                .stream().noneMatch(m -> "role_student_global".equals(m.getRole().getId()))) {
            com.school.erp.entity.auth.Role roleEntity = new com.school.erp.entity.auth.Role();
            roleEntity.setId("role_student_global");
            UserRoleMapping newMapping = new UserRoleMapping();
            newMapping.setSchoolId(school.getId());
            newMapping.setUser(user);
            newMapping.setRole(roleEntity);
            newMapping.setActive(true);
            userRoleMappingRepository.save(newMapping);
        }

        // Create Student record
        Student student = new Student();
        student.setUserId(user.getId());
        student.setName(request.name() != null ? request.name() : user.getName());
        student.setAdmissionNo(request.admissionNo() != null ? request.admissionNo() : generateAdmissionNo());
        student.setRollNumber(request.rollNumber());
        student.setSchool(school);
        student.setSectionId(request.sectionId());
        student.setAcademicYearId(request.academicYearId());
        student.setStatus("ACTIVE");
        student.setAdmissionDate(request.admissionDate() != null ? request.admissionDate() : LocalDate.now());

        // Set class via SchoolClass reference
        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(request.classId());
        student.setSchoolClass(schoolClass);

        studentRepository.save(student);

        return toUserResponse(user, false);
    }

    // ──────────────────────────────────────────────
    // LOGOUT
    // ──────────────────────────────────────────────

    @Transactional
    public void logout(String accessToken) {
        authSessionRepository.deleteByAccessToken(accessToken);
    }

    // ──────────────────────────────────────────────
    // REFRESH TOKEN
    // ──────────────────────────────────────────────

    @Transactional
    public AuthTokenResponse refreshToken(String refreshToken) {
        // Validate the refresh token
        if (!jwtUtil.isValidRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        AuthSession session = authSessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found — session may have been logged out"));

        User user = session.getUser();
        School school = session.getSchool();

        UserRoleMapping userSchoolRole = userRoleMappingRepository
                .findBySchoolIdAndUserIdAndIsActiveTrue(school.getId(), user.getId()).stream().findFirst()
                .orElseThrow(() -> new UnauthorizedException("User no longer has active access to this school"));

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), school.getId(), extractRoleEnum(userSchoolRole));
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), school.getId(), extractRoleEnum(userSchoolRole));

        // Update session
        session.setAccessToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        authSessionRepository.save(session);

        List<PermissionContextDto> permissions;
        if ("SUPER_ADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name()) || "SUPERADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name()) || "ADMIN".equalsIgnoreCase(extractRoleEnum(userSchoolRole).name())) {
            permissions = List.of(new PermissionContextDto("ALL", "GLOBAL"));
        } else {
            permissions = authorizationService.getEffectivePermissions(school.getId(), user.getId())
                    .stream()
                    .map(rp -> new PermissionContextDto(rp.getPermission().getPermissionKey(), rp.getScopeType()))
                    .toList();
        }

        return new AuthTokenResponse(
                user.getId(),
                school.getId(),
                null,   // not a student login
                user.getName() != null ? user.getName() : "",
                extractRoleEnum(userSchoolRole).name(),
                newAccessToken,
                newRefreshToken,
                permissions,
                List.of(new PersonaDto(extractRoleEnum(userSchoolRole).name(), extractRoleEnum(userSchoolRole).name(), RoleArchetype.STAFF)),
                new PersonaDto(extractRoleEnum(userSchoolRole).name(), extractRoleEnum(userSchoolRole).name(), RoleArchetype.STAFF)
        );
    }

    // ──────────────────────────────────────────────
    // SWITCH PERSONA
    // ──────────────────────────────────────────────

    @Transactional
    public AuthTokenResponse switchPersona(String roleId, String deviceInfo) {
        // Find current authenticated session context
        com.school.erp.security.AuthenticatedUser authUser = com.school.erp.security.AuthContextHolder.get();
        if (authUser == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        UserRoleMapping userSchoolRole = userRoleMappingRepository
                .findBySchoolIdAndUserIdAndIsActiveTrue(authUser.schoolId(), authUser.userId()).stream().findFirst()
                .orElseThrow(() -> new UnauthorizedException("Active school membership not found"));

        // Temporary stub for Persona Switching: In a full deployment, this validates the roleId against user_roles table.
        // For now, we simulate switching by refreshing the token for the requested role archetype.
        
        String newAccessToken = jwtUtil.generateAccessToken(authUser.userId(), authUser.schoolId(), parseRole(roleId));
        String newRefreshToken = jwtUtil.generateRefreshToken(authUser.userId(), authUser.schoolId(), parseRole(roleId));

        // Generate mock permission contexts depending on the persona
        List<PermissionContextDto> permissions = authorizationService.getEffectivePermissions(authUser.schoolId(), authUser.userId())
                    .stream()
                    .map(rp -> new PermissionContextDto(rp.getPermission().getPermissionKey(), rp.getScopeType()))
                    .toList();

        return new AuthTokenResponse(
                authUser.userId(),
                authUser.schoolId(),
                null,   // not a student login
                userSchoolRole.getUser().getName() != null ? userSchoolRole.getUser().getName() : "",
                roleId,
                newAccessToken,
                newRefreshToken,
                permissions,
                List.of(
                        new PersonaDto(roleId, roleId, RoleArchetype.STAFF)
                ),
                new PersonaDto(roleId, roleId, RoleArchetype.STAFF)
        );
    }

    // ──────────────────────────────────────────────
    // GET CURRENT USER (for /auth/me)
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id " + userId));
        return toUserResponse(user, false);
    }

    // ──────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────

    private User newUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setStatus("ACTIVE");
        return user;
    }

    private AuthUserResponse toUserResponse(User user, boolean newlyCreated) {
        List<UserSchoolResponse> schools = userRoleMappingRepository
                .findByUserIdAndIsActiveTrue(user.getId())
                .stream()
                .filter(r -> r.getSchoolId() != null)
                .map(this::toSchoolResponse)
                .toList();

        return new AuthUserResponse(
                user.getId(),
                user.getPhone(),
                user.getName(),
                user.getEmail(),
                user.getStatus(),
                newlyCreated,
                schools
        );
    }

    
    private UserRole extractRoleEnum(UserRoleMapping mapping) {
        if (mapping.getRole() == null) return UserRole.STUDENT;
        RoleArchetype archetype = mapping.getRole().getArchetype();
        if (archetype == RoleArchetype.SUPER_ADMIN) return UserRole.SUPER_ADMIN;
        if (archetype == RoleArchetype.SCHOOL_ADMIN) return UserRole.ADMIN;
        if (archetype == RoleArchetype.PARENT) return UserRole.PARENT;
        if (archetype == RoleArchetype.STAFF) return UserRole.TEACHER;
        return UserRole.STUDENT;
    }
    
    private UserSchoolResponse toSchoolResponse(UserRoleMapping mapping) {
        School school = schoolRepository.findById(mapping.getSchoolId())
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + mapping.getSchoolId()));
        return new UserSchoolResponse(
                school.getId(),
                school.getName(),
                school.getCode(),
                extractRoleEnum(mapping).name(),
                mapping.isActive() ? "ACTIVE" : "INACTIVE",
                school.getStatus()
        );
    }


    private UserRole parseRole(String roleName) {
        try {
            return UserRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + roleName
                    + ". Valid roles: SUPER_ADMIN, ADMIN, TEACHER, STUDENT, PARENT, STAFF");
        }
    }

    private String generateAdmissionNo() {
        return "ADM-" + System.currentTimeMillis();
    }
}
