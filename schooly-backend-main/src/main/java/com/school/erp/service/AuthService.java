package com.school.erp.service;

import com.school.erp.dto.auth.*;
import com.school.erp.entity.*;
import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.*;
import com.school.erp.security.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final AuthSessionRepository authSessionRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            UserSchoolRoleRepository userSchoolRoleRepository,
            AuthSessionRepository authSessionRepository,
            SchoolRepository schoolRepository,
            StudentRepository studentRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
        this.authSessionRepository = authSessionRepository;
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.jwtUtil = jwtUtil;
    }

    // ──────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────

    public AuthUserResponse loginOrSignup(String phone) {
        return userRepository.findByPhone(phone)
                .map(user -> toUserResponse(user, false))
                .orElseGet(() -> toUserResponse(userRepository.save(newUser(phone)), true));
    }

    // ──────────────────────────────────────────────
    // SCHOOLS & SELECT
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<UserSchoolResponse> getUserSchools(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found for id " + userId);
        }

        return userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(userId, "ACTIVE")
                .stream()
                .map(this::toSchoolResponse)
                .toList();
    }

    @Transactional
    public AuthTokenResponse selectSchool(Long userId, Long schoolId, String deviceInfo) {
        UserSchoolRole userSchoolRole = userSchoolRoleRepository
                .findByUserIdAndSchoolIdAndStatusIgnoreCase(userId, schoolId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active school membership not found for userId " + userId + " and schoolId " + schoolId
                ));

        String accessToken = jwtUtil.generateAccessToken(userId, schoolId, userSchoolRole.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userId, schoolId, userSchoolRole.getRole());

        AuthSession authSession = new AuthSession();
        authSession.setUser(userSchoolRole.getUser());
        authSession.setSchool(userSchoolRole.getSchool());
        authSession.setAccessToken(accessToken);
        authSession.setRefreshToken(refreshToken);
        authSession.setDeviceInfo(deviceInfo);
        authSessionRepository.save(authSession);

        return new AuthTokenResponse(
                userId,
                schoolId,
                userSchoolRole.getRole().name(),
                accessToken,
                refreshToken
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
        if (userSchoolRoleRepository.existsByUserIdAndSchoolIdAndRoleAndStatusIgnoreCase(
                user.getId(), school.getId(), role, "ACTIVE")) {
            throw new BadRequestException(
                    "User already has role " + role.name() + " in school " + school.getName()
            );
        }

        // Assign role
        UserSchoolRole userSchoolRole = new UserSchoolRole();
        userSchoolRole.setUser(user);
        userSchoolRole.setSchool(school);
        userSchoolRole.setRole(role);
        userSchoolRole.setStatus("ACTIVE");
        userSchoolRoleRepository.save(userSchoolRole);

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
        if (!userSchoolRoleRepository.existsByUserIdAndSchoolIdAndRoleAndStatusIgnoreCase(
                user.getId(), school.getId(), UserRole.STUDENT, "ACTIVE")) {
            UserSchoolRole studentRole = new UserSchoolRole();
            studentRole.setUser(user);
            studentRole.setSchool(school);
            studentRole.setRole(UserRole.STUDENT);
            studentRole.setStatus("ACTIVE");
            userSchoolRoleRepository.save(studentRole);
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

        UserSchoolRole userSchoolRole = userSchoolRoleRepository
                .findByUserIdAndSchoolIdAndStatusIgnoreCase(user.getId(), school.getId(), "ACTIVE")
                .orElseThrow(() -> new UnauthorizedException("User no longer has active access to this school"));

        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), school.getId(), userSchoolRole.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), school.getId(), userSchoolRole.getRole());

        // Update session
        session.setAccessToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        authSessionRepository.save(session);

        return new AuthTokenResponse(
                user.getId(),
                school.getId(),
                userSchoolRole.getRole().name(),
                newAccessToken,
                newRefreshToken
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
        List<UserSchoolResponse> schools = userSchoolRoleRepository
                .findByUserIdAndStatusIgnoreCase(user.getId(), "ACTIVE")
                .stream()
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

    private UserSchoolResponse toSchoolResponse(UserSchoolRole userSchoolRole) {
        return new UserSchoolResponse(
                userSchoolRole.getSchool().getId(),
                userSchoolRole.getSchool().getName(),
                userSchoolRole.getSchool().getCode(),
                userSchoolRole.getRole().name(),
                userSchoolRole.getStatus()
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
