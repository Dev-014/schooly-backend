package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.auth.AuthLoginRequest;
import com.school.erp.dto.auth.AuthTokenResponse;
import com.school.erp.dto.auth.AuthUserResponse;
import com.school.erp.dto.auth.SelectSchoolRequest;
import com.school.erp.dto.auth.UserSchoolResponse;
import com.school.erp.service.AuthService;
import com.school.erp.dto.auth.LoginVerifyResponse;
import com.school.erp.dto.auth.OtpVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthUserResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.loginOrSignup(request.phone()),
                "Login/signup processed successfully"
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<LoginVerifyResponse>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String deviceInfo = userAgent == null ? null : "{\"userAgent\":\"" + userAgent.replace("\"", "\\\"") + "\"}";
        return ResponseEntity.ok(ApiResponse.success(
                authService.verifyOtp(request, deviceInfo),
                "OTP verified successfully"
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/schools")
    public ResponseEntity<ApiResponse<List<UserSchoolResponse>>> getUserSchools(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.getUserSchools(userId),
                "User schools fetched successfully"
        ));
    }

    @PostMapping("/select-school")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> selectSchool(
            @Valid @RequestBody SelectSchoolRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String userAgent = httpServletRequest.getHeader("User-Agent");
        String deviceInfo = userAgent == null ? null : "{\"userAgent\":\"" + userAgent.replace("\"", "\\\"") + "\"}";
        return ResponseEntity.ok(ApiResponse.success(
                authService.selectSchool(request.userId(), request.schoolId(), deviceInfo),
                "School selected successfully"
        ));
    }
}
