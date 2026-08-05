package com.school.erp.service.auth;

import com.school.erp.dto.auth.AccountRequestSubmitRequest;
import com.school.erp.dto.auth.AuthTokenResponse;
import com.school.erp.dto.auth.PasswordResetRequest;
import com.school.erp.dto.auth.PasswordResetSubmitRequest;
import com.school.erp.entity.AccountRequest;
import com.school.erp.entity.PasswordResetToken;
import com.school.erp.entity.School;
import com.school.erp.entity.User;
import com.school.erp.entity.UserRole;
import com.school.erp.repository.AccountRequestRepository;
import com.school.erp.repository.ImpersonationSessionRepository;
import com.school.erp.repository.PasswordResetTokenRepository;
import com.school.erp.repository.SchoolRepository;
import com.school.erp.repository.UserRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthManagementService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final SchoolRepository schoolRepository;
    private final ImpersonationSessionRepository impersonationSessionRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public String requestPasswordReset(PasswordResetRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user found with email " + request.getEmail()));

        // Generate a random token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setRequestIp(getClientIp(httpRequest));
        resetToken.setDeviceInfo(httpRequest.getHeader("User-Agent"));

        passwordResetTokenRepository.save(resetToken);

        // In a real application, you would send an email here.
        return token;
    }

    @Transactional
    public void submitPasswordReset(PasswordResetSubmitRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (!resetToken.getStatus().equals("PENDING") || resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetToken.setStatus("EXPIRED");
            passwordResetTokenRepository.save(resetToken);
            throw new IllegalArgumentException("Token is expired or already used");
        }

        User user = resetToken.getUser();
        // Normally you'd hash the new password using PasswordEncoder
        user.setPasswordHash("$2a$10$temporaryHashForNow");
        userRepository.save(user);

        resetToken.setStatus("USED");
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void submitAccountRequest(AccountRequestSubmitRequest request) {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid school selected"));

        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setSchool(school);
        accountRequest.setRequestType("ACCOUNT_CREATION");
        accountRequest.setRequesterName(request.getName());
        accountRequest.setRequesterEmail(request.getEmail());
        accountRequest.setRequesterPhone(request.getPhone());
        accountRequest.setRequestedRole(request.getRequestedRole().toUpperCase());
        accountRequest.setDescription(request.getDescription());
        accountRequest.setStatus("PENDING");
        accountRequestRepository.save(accountRequest);
    }

    @Transactional
    public AuthTokenResponse stopImpersonation() {
        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (currentUser == null || currentUser.impersonatorId() == null) {
            throw new IllegalArgumentException("Not currently impersonating anyone");
        }

        User impersonator = userRepository.findById(currentUser.impersonatorId())
                .orElseThrow(() -> new IllegalArgumentException("Impersonator not found"));

        impersonationSessionRepository.findFirstByOriginalUserIdAndStatusOrderByStartedAtDesc(impersonator.getId(), "ACTIVE")
                .ifPresent(session -> {
                    session.setStatus("STOPPED");
                    session.setEndedAt(LocalDateTime.now());
                    impersonationSessionRepository.save(session);
                });

        // Here we just hardcode UserRole.SUPER_ADMIN since only super admins can impersonate for now.
        // Ideally we fetch the active role from UserSchoolRole
        String accessToken = jwtUtil.generateAccessToken(impersonator.getId(), null, UserRole.SUPER_ADMIN, null);
        String refreshToken = jwtUtil.generateRefreshToken(impersonator.getId(), null, UserRole.SUPER_ADMIN);

        return new AuthTokenResponse(
                impersonator.getId(),
                null,
                UserRole.SUPER_ADMIN.name(),
                accessToken,
                refreshToken
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
