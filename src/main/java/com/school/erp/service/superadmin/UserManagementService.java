package com.school.erp.service.superadmin;

import com.school.erp.dto.usermanagement.*;
import com.school.erp.entity.*;
import com.school.erp.repository.*;
import lombok.RequiredArgsConstructor;
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
    public Page<UserDTO> searchUsers(String query, Pageable pageable) {
        // Implement custom query later
        return Page.empty(pageable);
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
    public Page<UserActivityLogDTO> getActivityLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByTimestampDesc(pageable)
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
                        .build());
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
}
