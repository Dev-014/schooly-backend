package com.school.erp.service.superadmin;

import com.school.erp.dto.usermanagement.*;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.security.JwtUtil;
import com.school.erp.service.auth.RoleSyncService;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final UserLoginHistoryRepository loginHistoryRepository;
    private final UserActivityLogRepository activityLogRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final SchoolRepository schoolRepository;
    private final ImpersonationSessionRepository impersonationSessionRepository;
    private final JwtUtil jwtUtil;
    private final RoleSyncService roleSyncService;

    @Transactional(readOnly = true)
    public UserDashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userSchoolRoleRepository.count(); // Approximation for now
        long inactiveUsers = Math.max(0, totalUsers - activeUsers);
        
        long todaysLogins = loginHistoryRepository.countByLoginTimeAfter(LocalDateTime.now().toLocalDate().atStartOfDay());
        
        return UserDashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .lockedAccounts(0)
                .todaysLogins(todaysLogins)
                .passwordResetRequests(accountRequestRepository.countByStatus("PENDING"))
                .newUsersThisMonth(0)
                .onlineUsers(0)
                .usersByRole(Collections.emptyList())
                .usersByStatus(Collections.emptyList())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getSchoolUsers(Long schoolId, Pageable pageable) {
        // Implement custom query later, for now return empty
        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(String query, Long schoolId, String role, String status, Pageable pageable) {
        // Implement custom query later or return mock for now.
        // As a quick fix for full-stack functionality without complex dynamic queries right away:
        List<User> allUsers = userRepository.findAll();
        List<UserDTO> dtoList = allUsers.stream()
                .filter(u -> query == null || query.isEmpty() 
                        || (u.getName() != null && u.getName().toLowerCase().contains(query.toLowerCase())) 
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase())) 
                        || (u.getPhone() != null && u.getPhone().contains(query)))
                .filter(u -> status == null || status.isEmpty() || (u.getStatus() != null && u.getStatus().equalsIgnoreCase(status)))
                .map(u -> {
                    // Get roles for this user
                    List<UserSchoolRole> roles = userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(u.getId(), "ACTIVE");
                    UserSchoolRole primaryRole = roles.isEmpty() ? null : roles.get(0);
                    
                    String mappedRole = primaryRole != null ? primaryRole.getRole().name() : "NO_ROLE";
                    Long mappedSchoolId = (primaryRole != null && primaryRole.getSchool() != null) ? primaryRole.getSchool().getId() : null;
                    String mappedSchoolName = (primaryRole != null && primaryRole.getSchool() != null) ? primaryRole.getSchool().getName() : null;
                    
                    // Check if super admin implicitly
                    if (primaryRole == null && "SUPER_ADMIN".equals(u.getStatus())) { // Or some other check if needed
                        // Actually super admins are often in UserSchoolRole with school=null
                    }
                    
                    return UserDTO.builder()
                        .id(u.getId())
                        .name(u.getName() != null ? u.getName() : "Unknown User")
                        .email(u.getEmail())
                        .phone(u.getPhone())
                        .role(mappedRole)
                        .status(u.getStatus())
                        .schoolId(mappedSchoolId)
                        .schoolName(mappedSchoolName)
                        .lastLogin(LocalDateTime.now().minusDays(1))
                        .authStatus(u.getPasswordHash() != null && !u.getPasswordHash().isEmpty() ? "MFA_ENABLED" : "PENDING_SETUP")
                        .build();
                })
                .filter(dto -> schoolId == null || (dto.getSchoolId() != null && dto.getSchoolId().equals(schoolId)))
                .filter(dto -> role == null || role.isEmpty() || dto.getRole().equalsIgnoreCase(role))
                .collect(Collectors.toList());

        // Simple manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        if (start > dtoList.size()) return Page.empty(pageable);
        return new org.springframework.data.domain.PageImpl<>(dtoList.subList(start, end), pageable, dtoList.size());
    }

    @Transactional
    public UserDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        user.setPasswordHash("$2a$10$temporaryHashForNow"); // Simplified for now
        user = userRepository.save(user);

        UserSchoolRole usr = new UserSchoolRole();
        usr.setUser(user);
        usr.setSchool(school);
        usr.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        usr.setStatus("ACTIVE");
        userSchoolRoleRepository.save(usr);
        roleSyncService.syncUserSchoolRole(usr);

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(usr.getRole().name())
                .status(user.getStatus())
                .schoolId(school.getId())
                .schoolName(school.getName())
                .build();
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getPhone().equals(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty() && !request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userRepository.save(user);

        // Update role and school
        List<UserSchoolRole> roles = userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(userId, "ACTIVE");
        UserSchoolRole usr;
        if (roles.isEmpty()) {
            usr = new UserSchoolRole();
            usr.setUser(user);
            usr.setStatus("ACTIVE");
        } else {
            usr = roles.get(0); // For simplicity, take the first role
        }

        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));

        usr.setSchool(school);
        usr.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        userSchoolRoleRepository.save(usr);
        roleSyncService.syncUserSchoolRole(usr);

        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(usr.getRole().name())
                .status(user.getStatus())
                .schoolId(school.getId())
                .schoolName(school.getName())
                .build();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // Instead of hard delete, deactivate
        user.setStatus("INACTIVE");
        userRepository.save(user);
    }

    @Transactional
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setStatus(status.toUpperCase());
        userRepository.save(user);
    }

    @Transactional
    public String resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";
        StringBuilder pass = new StringBuilder();
        java.util.Random rand = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            pass.append(chars.charAt(rand.nextInt(chars.length())));
        }
        
        // Normally use BCryptPasswordEncoder
        user.setPasswordHash("$2a$10$temporaryHashForNow");
        userRepository.save(user);

        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        
        List<UserSchoolRole> userRoles = userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(userId, "ACTIVE");
        log.setRole(userRoles.isEmpty() ? "SUPER_ADMIN" : userRoles.get(0).getRole().name());
        if (!userRoles.isEmpty() && userRoles.get(0).getSchool() != null) {
            log.setSchool(userRoles.get(0).getSchool());
        }
        
        log.setModule("SECURITY");
        log.setAction("PASSWORD_RESET_GENERATED");
        log.setTimestamp(java.time.LocalDateTime.now());
        activityLogRepository.save(log);

        return pass.toString();
    }

    public List<String> getAvailableRoles() {
        return java.util.Arrays.stream(UserRole.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserLoginHistoryDTO> getLoginHistory(Pageable pageable) {
        return loginHistoryRepository.findAllByOrderByLoginTimeDesc(pageable)
                .map(log -> UserLoginHistoryDTO.builder()
                        .id(log.getId())
                        .userId(log.getUser().getId())
                        .userName(log.getUser().getName())
                        .schoolId(log.getSchool() != null ? log.getSchool().getId() : null)
                        .schoolName(log.getSchool() != null ? log.getSchool().getName() : null)
                        .device(log.getDevice())
                        .browser(log.getBrowser())
                        .ipAddress(log.getIpAddress())
                        .status(log.getStatus())
                        .loginTime(log.getLoginTime())
                        .logoutTime(log.getLogoutTime())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<UserActivityLogDTO> getActivityLogs(String query, Long schoolId, String role, String module, Pageable pageable) {
        // Fetch all and filter in memory for now to avoid writing a complex Specification builder just for this mock
        // In a real production app, this would use JPA Specifications
        List<UserActivityLog> allLogs = activityLogRepository.findAll();
        List<UserActivityLogDTO> filteredLogs = allLogs.stream()
                .filter(log -> query == null || query.isEmpty() 
                        || (log.getUser() != null && log.getUser().getName() != null && log.getUser().getName().toLowerCase().contains(query.toLowerCase())) 
                        || (log.getAction() != null && log.getAction().toLowerCase().contains(query.toLowerCase())))
                .filter(log -> schoolId == null || log.getSchool().getId().equals(schoolId))
                .filter(log -> role == null || role.isEmpty() || log.getRole().equalsIgnoreCase(role))
                .filter(log -> module == null || module.isEmpty() || log.getModule().equalsIgnoreCase(module))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .map(log -> UserActivityLogDTO.builder()
                        .id(log.getId())
                        .userId(log.getUser().getId())
                        .userName(log.getUser().getName())
                        .schoolId(log.getSchool().getId())
                        .schoolName(log.getSchool().getName())
                        .role(log.getRole())
                        .module(log.getModule())
                        .action(log.getAction())
                        .ipAddress(log.getIpAddress())
                        .timestamp(log.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredLogs.size());
        if (start > filteredLogs.size()) return Page.empty(pageable);
        return new org.springframework.data.domain.PageImpl<>(filteredLogs.subList(start, end), pageable, filteredLogs.size());
    }

    @Transactional(readOnly = true)
    public Page<AccountRequestDTO> getAccountRequests(Pageable pageable) {
        return accountRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(req -> AccountRequestDTO.builder()
                        .id(req.getId())
                        .schoolId(req.getSchool().getId())
                        .schoolName(req.getSchool().getName())
                        .userId(req.getUser() != null ? req.getUser().getId() : null)
                        .userName(req.getUser() != null ? req.getUser().getName() : null)
                        .requestType(req.getRequestType())
                        .description(req.getDescription())
                        .status(req.getStatus())
                        .createdAt(req.getCreatedAt())
                        .resolvedAt(req.getResolvedAt())
                        .resolutionNotes(req.getResolutionNotes())
                        .build());
    }
    @Transactional
    public void approveAccountRequest(Long id) {
        AccountRequest request = accountRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }

        if (userRepository.existsByPhone(request.getRequesterPhone()) || userRepository.existsByEmail(request.getRequesterEmail())) {
            throw new IllegalArgumentException("User with this email or phone already exists");
        }

        // Create the user
        User user = new User();
        user.setName(request.getRequesterName());
        user.setEmail(request.getRequesterEmail());
        user.setPhone(request.getRequesterPhone());
        user.setPasswordHash("$2a$10$temporaryHashForNow"); // Simplified for now
        user.setStatus("ACTIVE");
        user = userRepository.save(user);

        // Assign Role and School
        UserSchoolRole usr = new UserSchoolRole();
        usr.setUser(user);
        usr.setSchool(request.getSchool());
        usr.setRole(UserRole.valueOf(request.getRequestedRole().toUpperCase()));
        usr.setStatus("ACTIVE");
        userSchoolRoleRepository.save(usr);
        roleSyncService.syncUserSchoolRole(usr);

        request.setUser(user);
        request.setStatus("APPROVED");
        request.setResolvedAt(LocalDateTime.now());
        accountRequestRepository.save(request);

        // Log activity
        UserActivityLog log = new UserActivityLog();
        log.setUser(user);
        log.setSchool(request.getSchool());
        log.setRole(usr.getRole().name());
        log.setModule("ACCOUNT_REQUESTS");
        log.setAction("APPROVED_ACCOUNT_REQUEST_" + id);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    @Transactional
    public void rejectAccountRequest(Long id, String reason) {
        AccountRequest request = accountRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account Request not found"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }

        request.setStatus("REJECTED");
        request.setRejectReason(reason);
        request.setResolvedAt(LocalDateTime.now());
        accountRequestRepository.save(request);
    }
    @Transactional
    public ImpersonationResponseDTO impersonateUser(Long targetUserId) {
        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (currentUser == null || !UserRole.SUPER_ADMIN.equals(currentUser.role())) {
            throw new UnauthorizedException("Only Super Admins can impersonate users");
        }

        if (currentUser.impersonatorId() != null) {
            throw new UnauthorizedException("Nested impersonation is strictly prohibited");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        if (!"ACTIVE".equalsIgnoreCase(targetUser.getStatus())) {
            throw new UnauthorizedException("Cannot impersonate an inactive or locked user");
        }

        // Determine target role (just taking the first active role for simplicity)
        List<UserSchoolRole> targetRoles = userSchoolRoleRepository.findByUserIdAndStatusIgnoreCase(targetUserId, "ACTIVE");
        
        // Block impersonating other Super Admins
        boolean isTargetSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(targetUser.getStatus()) && targetRoles.isEmpty(); // Using the legacy status logic
        if (targetRoles.stream().anyMatch(r -> UserRole.SUPER_ADMIN.equals(r.getRole())) || isTargetSuperAdmin) {
            throw new UnauthorizedException("Cannot impersonate another Super Admin");
        }

        UserRole targetRole = targetRoles.isEmpty() ? UserRole.STUDENT : targetRoles.get(0).getRole();
        Long targetSchoolId = targetRoles.isEmpty() || targetRoles.get(0).getSchool() == null ? null : targetRoles.get(0).getSchool().getId();

        String accessToken = jwtUtil.generateAccessToken(targetUserId, targetSchoolId, targetRole, currentUser.userId());
        String refreshToken = jwtUtil.generateRefreshToken(targetUserId, targetSchoolId, targetRole);

        // Get IP and Device
        String ipAddress = null;
        String deviceInfo = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest req = attributes.getRequest();
                ipAddress = req.getRemoteAddr();
                deviceInfo = req.getHeader("User-Agent");
            }
        } catch (Exception e) {
            // Ignore if outside request context
        }

        String sessionId = java.util.UUID.randomUUID().toString();

        ImpersonationSession session = new ImpersonationSession();
        session.setOriginalUser(userRepository.findById(currentUser.userId()).orElse(null));
        session.setImpersonatedUser(targetUser);
        session.setSessionId(sessionId);
        session.setIpAddress(ipAddress);
        // Wrap device info in JSON explicitly if needed, but String user-agent might violate JSONB if not valid JSON string.
        // Actually, just storing it as a plain string inside JSON quotes:
        session.setDeviceInfo("\"" + (deviceInfo != null ? deviceInfo.replace("\"", "\\\"") : "Unknown") + "\"");
        session.setStatus("ACTIVE");
        impersonationSessionRepository.save(session);

        // Optional: Log this impersonation activity
        UserActivityLog log = new UserActivityLog();
        log.setUser(session.getOriginalUser());
        log.setRole(currentUser.role().name());
        if (!targetRoles.isEmpty() && targetRoles.get(0).getSchool() != null) {
            log.setSchool(targetRoles.get(0).getSchool());
        }
        log.setModule("USER_MANAGEMENT");
        log.setAction("IMPERSONATE_USER_ID_" + targetUserId);
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);

        return ImpersonationResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .targetUserId(targetUserId)
                .targetUserName(targetUser.getName())
                .targetUserRole(targetRole.name())
                .sessionId(sessionId)
                .build();
    }
}
