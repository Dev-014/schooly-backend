package com.school.erp.service;

import com.school.erp.dto.auth.AuthTokenResponse;
import com.school.erp.dto.auth.AuthUserResponse;
import com.school.erp.dto.auth.UserSchoolResponse;
import com.school.erp.entity.AuthSession;
import com.school.erp.entity.User;
import com.school.erp.entity.UserSchoolRole;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.AuthSessionRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.repository.UserSchoolRoleRepository;
import com.school.erp.security.JwtUtil;
import com.school.erp.dto.auth.LoginVerifyResponse;
import com.school.erp.dto.auth.OtpVerifyRequest;
import com.school.erp.dto.auth.StudentSummaryDto;
import com.school.erp.entity.Student;
import com.school.erp.entity.StudentParent;
import com.school.erp.entity.UserRole;
import com.school.erp.exception.UnauthorizedException;
import com.school.erp.repository.StudentParentRepository;
import com.school.erp.repository.StudentRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final AuthSessionRepository authSessionRepository;
    private final StudentParentRepository studentParentRepository;
    private final StudentRepository studentRepository;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            UserSchoolRoleRepository userSchoolRoleRepository,
            AuthSessionRepository authSessionRepository,
            StudentParentRepository studentParentRepository,
            StudentRepository studentRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
        this.authSessionRepository = authSessionRepository;
        this.studentParentRepository = studentParentRepository;
        this.studentRepository = studentRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthUserResponse loginOrSignup(String phone) {
        return userRepository.findByPhone(phone)
                .map(user -> toUserResponse(user, false))
                .orElseGet(() -> toUserResponse(userRepository.save(newUser(phone)), true));
    }

    @Transactional
    public LoginVerifyResponse verifyOtp(OtpVerifyRequest request, String deviceInfo) {
        if (!"1111".equals(request.otp())) {
            throw new UnauthorizedException("Invalid 4-digit verification code");
        }

        User user = userRepository.findByPhone(request.phone())
                .orElseThrow(() -> new UnauthorizedException("User not found for phone " + request.phone()));

        List<UserSchoolRole> userRoles = userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(user.getId(), "ACTIVE");
        List<String> roleNames = userRoles.stream().map(r -> r.getRole().name()).distinct().toList();
        if (roleNames.isEmpty()) {
            roleNames = List.of("STUDENT"); // Fallback for basic users
        }

        String primaryRole = determinePrimaryRole(roleNames);
        List<UserSchoolResponse> schools = userRoles.stream()
                .filter(r -> r.getSchool() != null)
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
                    students.add(new StudentSummaryDto(s.getId(), s.getName(), s.getAdmissionNo(), className, schoolId));
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
                students.add(new StudentSummaryDto(s.getId(), s.getName(), s.getAdmissionNo(), className, schoolId));
            }
            if (students.size() > 1) {
                requiresStudentSelection = true;
            }
        }

        String accessToken = null;
        String refreshToken = null;

        if (!requiresSchoolSelection) {
            Long targetSchoolId = !schools.isEmpty() ? schools.get(0).schoolId() : null;
            UserRole targetRole = userRoles.isEmpty() ? UserRole.STUDENT : userRoles.get(0).getRole();
            for (UserSchoolRole usr : userRoles) {
                if (usr.getRole().name().equals(primaryRole)) {
                    targetRole = usr.getRole();
                    targetSchoolId = usr.getSchool() != null ? usr.getSchool().getId() : null;
                    break;
                }
            }
            accessToken = jwtUtil.generateAccessToken(user.getId(), targetSchoolId, targetRole);
            refreshToken = jwtUtil.generateRefreshToken(user.getId(), targetSchoolId, targetRole);

            AuthSession authSession = new AuthSession();
            authSession.setUser(user);
            if (!schools.isEmpty() && targetSchoolId != null) {
                for (UserSchoolRole usr : userRoles) {
                    if (usr.getSchool() != null && Objects.equals(usr.getSchool().getId(), targetSchoolId)) {
                        authSession.setSchool(usr.getSchool());
                        break;
                    }
                }
            }
            authSession.setAccessToken(accessToken);
            authSession.setRefreshToken(refreshToken);
            authSessionRepository.save(authSession);
        }

        List<String> permissions = List.of("ALL");

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
                permissions
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

    @Transactional(readOnly = true)
    public List<UserSchoolResponse> getUserSchools(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found for id " + userId);
        }

        return userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(userId, "ACTIVE")
                .stream()
                .filter(r -> r.getSchool() != null)
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
        authSession.setDeviceInfo(deviceInfo != null ? deviceInfo : null);
        authSessionRepository.save(authSession);

        return new AuthTokenResponse(
                userId,
                schoolId,
                userSchoolRole.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    private User newUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setStatus("ACTIVE");
        return user;
    }

    private AuthUserResponse toUserResponse(User user, boolean newlyCreated) {
        return new AuthUserResponse(
                user.getId(),
                user.getPhone(),
                user.getName(),
                user.getEmail(),
                user.getStatus(),
                newlyCreated
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
}
