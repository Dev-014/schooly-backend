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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final AuthSessionRepository authSessionRepository;
    private final JwtUtil jwtUtil;

    public AuthService(
            UserRepository userRepository,
            UserSchoolRoleRepository userSchoolRoleRepository,
            AuthSessionRepository authSessionRepository,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.userSchoolRoleRepository = userSchoolRoleRepository;
        this.authSessionRepository = authSessionRepository;
        this.jwtUtil = jwtUtil;
    }

    public AuthUserResponse loginOrSignup(String phone) {
        return userRepository.findByPhone(phone)
                .map(user -> toUserResponse(user, false))
                .orElseGet(() -> toUserResponse(userRepository.save(newUser(phone)), true));
    }

    public List<UserSchoolResponse> getUserSchools(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found for id " + userId);
        }

        return userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(userId, "ACTIVE")
                .stream()
                .map(this::toSchoolResponse)
                .toList();
    }

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
