package com.school.erp.controller;

import com.school.erp.api.ApiResponse;
import com.school.erp.dto.auth.*;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login / Signup", description = "Authenticates user by phone. Creates account if new. Returns user with roles per school.")
    public ResponseEntity<ApiResponse<AuthUserResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.loginOrSignup(request.phone()),
                "Login/signup processed successfully"
        ));
    }

    @GetMapping("/schools")
    @Operation(summary = "Get User Schools", description = "Returns all schools and roles for the given user")
    public ResponseEntity<ApiResponse<List<UserSchoolResponse>>> getUserSchools(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.getUserSchools(userId),
                "User schools fetched successfully"
        ));
    }

    @PostMapping("/select-school")
    @Operation(summary = "Select School", description = "Selects a school for the user session and returns JWT tokens")
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

    @PostMapping("/register")
    @Operation(summary = "Register User to School", description = "Registers a user with a specific role (ADMIN, TEACHER, STUDENT, PARENT, STAFF) to a school")
    public ResponseEntity<ApiResponse<AuthUserResponse>> registerUser(
            @Valid @RequestBody RegisterUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                authService.registerUserToSchool(request),
                "User registered to school successfully"
        ));
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register Student", description = "Creates user account + student record + assigns STUDENT role to school in one step")
    public ResponseEntity<ApiResponse<AuthUserResponse>> registerStudent(
            @Valid @RequestBody RegisterStudentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                authService.registerStudent(request),
                "Student registered successfully"
        ));
    }

    @GetMapping("/me")
    @Operation(summary = "Get Current User", description = "Returns the authenticated user's profile with all school-role mappings")
    public ResponseEntity<ApiResponse<AuthUserResponse>> getCurrentUser() {
        AuthenticatedUser authUser = AuthContextHolder.get();
        return ResponseEntity.ok(ApiResponse.success(
                authService.getCurrentUser(authUser.userId()),
                "Current user fetched successfully"
        ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates the current session by deleting the auth session")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            authService.logout(header.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Generates new access and refresh tokens using a valid refresh token")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.refreshToken(request.refreshToken()),
                "Token refreshed successfully"
        ));
    }
}
