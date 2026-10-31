package com.school.erp.service.notification.impl;

import com.school.erp.dto.notification.CreateNotificationRequest;
import com.school.erp.dto.notification.NotificationCountResponse;
import com.school.erp.dto.notification.NotificationResponse;
import com.school.erp.entity.academic.ClassTeacherAssignment;
import com.school.erp.entity.auth.User;
import com.school.erp.entity.auth.UserAssignment;
import com.school.erp.entity.auth.UserRole;
import com.school.erp.entity.auth.UserSchoolRole;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.notification.Notification;
import com.school.erp.entity.notification.NotificationCategory;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.student.Student;
import com.school.erp.entity.student.StudentParent;
import com.school.erp.exception.ResourceNotFoundException;
import com.school.erp.repository.academic.ClassTeacherAssignmentRepository;
import com.school.erp.repository.auth.UserAssignmentRepository;
import com.school.erp.repository.auth.UserRepository;
import com.school.erp.repository.auth.UserSchoolRoleRepository;
import com.school.erp.repository.notification.NotificationRepository;
import com.school.erp.repository.student.StudentParentRepository;
import com.school.erp.repository.student.StudentRepository;
import com.school.erp.security.AuthContextHolder;
import com.school.erp.security.AuthenticatedUser;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserSchoolRoleRepository userSchoolRoleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ClassTeacherAssignmentRepository classTeacherAssignmentRepository;
    private final UserAssignmentRepository userAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(
            Long userId,
            Long schoolId,
            Boolean isRead,
            NotificationCategory category,
            NotificationType notificationType,
            NotificationPriority priority,
            Pageable pageable
    ) {
        return getNotifications(userId, schoolId, null, isRead, category, notificationType, priority, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(
            Long userId,
            Long schoolId,
            String targetRole,
            Boolean isRead,
            NotificationCategory category,
            NotificationType notificationType,
            NotificationPriority priority,
            Pageable pageable
    ) {
        String effectiveRole = resolveFilterRole(userId, schoolId, targetRole);
        Page<Notification> page = (effectiveRole != null)
                ? notificationRepository.filterNotificationsByRole(
                        schoolId, userId, effectiveRole, isRead, category, notificationType, priority, pageable)
                : notificationRepository.filterNotifications(
                        schoolId, userId, isRead, category, notificationType, priority, pageable);
        return page.map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadSummary(Long userId, Long schoolId) {
        return getUnreadSummary(userId, schoolId, null);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadSummary(Long userId, Long schoolId, String targetRole) {
        String effectiveRole = resolveFilterRole(userId, schoolId, targetRole);
        long unreadCount = (effectiveRole != null)
                ? notificationRepository.countUnreadByRole(schoolId, userId, effectiveRole)
                : notificationRepository.countBySchoolIdAndUserIdAndIsReadFalse(schoolId, userId);
        long totalCount = (effectiveRole != null)
                ? notificationRepository.countTotalByRole(schoolId, userId, effectiveRole)
                : notificationRepository.countBySchoolIdAndUserId(schoolId, userId);

        List<Object[]> categoryCounts = (effectiveRole != null)
                ? notificationRepository.countUnreadGroupedByCategoryAndRole(schoolId, userId, effectiveRole)
                : notificationRepository.countUnreadGroupedByCategory(schoolId, userId);

        Map<String, Long> unreadByCategory = new HashMap<>();
        for (Object[] row : categoryCounts) {
            if (row[0] != null && row[1] != null) {
                unreadByCategory.put(row[0].toString(), (Long) row[1]);
            }
        }

        return new NotificationCountResponse(unreadCount, totalCount, unreadByCategory);
    }

    private String resolveFilterRole(Long userId, Long schoolId, String requestedRole) {
        if (requestedRole != null && !requestedRole.isBlank() && !"ALL".equalsIgnoreCase(requestedRole.trim())) {
            return requestedRole.trim().toUpperCase();
        }
        // If user holds multiple active roles in this school, scope to their active session persona
        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (currentUser != null && currentUser.role() != null && currentUser.userId() != null && currentUser.userId().equals(userId)) {
            List<UserSchoolRole> roles = userSchoolRoleRepository.findByUserIdAndSchoolId(userId, schoolId);
            long distinctRoles = roles.stream()
                    .filter(r -> "ACTIVE".equalsIgnoreCase(r.getStatus()) && r.getRole() != null)
                    .map(UserSchoolRole::getRole)
                    .distinct()
                    .count();
            if (distinctRoles > 1) {
                return currentUser.role().name();
            }
        }
        return null;
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId, Long schoolId) {
        Notification notification = notificationRepository.findByIdAndSchoolIdAndUserId(id, schoolId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return NotificationResponse.fromEntity(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId, Long schoolId) {
        return notificationRepository.markAllAsRead(schoolId, userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int markBatchAsRead(List<Long> ids, Long userId, Long schoolId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return notificationRepository.markBatchAsRead(schoolId, userId, ids, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long userId, Long schoolId) {
        Notification notification = notificationRepository.findByIdAndSchoolIdAndUserId(id, schoolId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public int clearReadNotifications(Long userId, Long schoolId) {
        return notificationRepository.deleteReadNotifications(schoolId, userId);
    }

    @Override
    @Transactional
    public List<NotificationResponse> createNotification(
            CreateNotificationRequest request,
            Long schoolId,
            AuthenticatedUser currentUser
    ) {
        NotificationCategory resolvedCategory = request.category() != null
                ? request.category()
                : resolveCategoryFromType(request.notificationType());

        NotificationPriority resolvedPriority = request.priority() != null
                ? request.priority()
                : NotificationPriority.NORMAL;

        Long actorId = currentUser != null ? currentUser.userId() : null;
        String actorRole = currentUser != null && currentUser.role() != null ? currentUser.role().name() : "SYSTEM";
        String actorName = resolveActorName(actorId, actorRole);

        // Case 1: Targeted to a single user
        if (request.userId() != null) {
            Notification saved = sendDirectNotification(
                    schoolId,
                    request.userId(),
                    request.targetRole(),
                    request.notificationType(),
                    resolvedCategory,
                    resolvedPriority,
                    request.title(),
                    request.message(),
                    actorId,
                    actorRole,
                    actorName,
                    request.actionUrl(),
                    request.actionType(),
                    request.entityType(),
                    request.entityId(),
                    request.metadata()
            );
            return List.of(NotificationResponse.fromEntity(saved));
        }

        // Case 2: Broadcast to all active users with targetRole in school
        List<Notification> created = broadcastToRole(
                schoolId,
                request.targetRole(),
                request.notificationType(),
                resolvedCategory,
                resolvedPriority,
                request.title(),
                request.message(),
                actorId,
                actorRole,
                actorName,
                request.actionUrl(),
                request.actionType(),
                request.entityType(),
                request.entityId(),
                request.metadata()
        );

        return created.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Notification sendDirectNotification(
            Long schoolId,
            Long recipientUserId,
            String targetRole,
            NotificationType notificationType,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            Long actorId,
            String actorRole,
            String actorName,
            String actionUrl,
            String actionType,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null || recipientUserId == null) {
            return null;
        }
        ActorContext actor = resolveActorContext(actorId, actorRole, actorName);
        Notification notification = Notification.builder()
                .schoolId(schoolId)
                .userId(recipientUserId)
                .targetRole(targetRole != null ? targetRole.toUpperCase() : "GENERAL")
                .title(title)
                .message(message)
                .notificationType(notificationType)
                .category(category != null ? category : resolveCategoryFromType(notificationType))
                .priority(priority != null ? priority : NotificationPriority.NORMAL)
                .isRead(false)
                .actorId(actor.actorId())
                .actorRole(actor.actorRole())
                .actorName(actor.actorName())
                .actionUrl(actionUrl)
                .actionType(actionType != null ? actionType : "NAVIGATE")
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Dispatched notification [{}] to user [{}] ({}) for school [{}]",
                notificationType, recipientUserId, notification.getTargetRole(), schoolId);
        return saved;
    }

    @Override
    @Transactional
    public List<Notification> broadcastToRole(
            Long schoolId,
            String targetRole,
            NotificationType notificationType,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            Long actorId,
            String actorRole,
            String actorName,
            String actionUrl,
            String actionType,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        String roleStr = targetRole != null ? targetRole.toUpperCase().trim() : "ADMIN";
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            userRole = UserRole.ADMIN;
        }

        List<UserSchoolRole> activeSchoolRoles = userSchoolRoleRepository
                .findBySchoolIdAndRoleAndStatusIgnoreCase(schoolId, userRole, "ACTIVE");

        if (activeSchoolRoles.isEmpty() && userRole == UserRole.SUPER_ADMIN) {
            activeSchoolRoles = userSchoolRoleRepository.findByRole(UserRole.SUPER_ADMIN);
        }

        Set<Long> userIds = activeSchoolRoles.stream()
                .filter(usr -> usr.getUser() != null && usr.getUser().getId() != null)
                .map(usr -> usr.getUser().getId())
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            log.debug("No active users found for role [{}] in school [{}], skipping broadcast", roleStr, schoolId);
            return Collections.emptyList();
        }

        ActorContext actor = resolveActorContext(actorId, actorRole, actorName);
        NotificationCategory cat = category != null ? category : resolveCategoryFromType(notificationType);
        NotificationPriority prio = priority != null ? priority : NotificationPriority.NORMAL;

        List<Notification> toSave = userIds.stream()
                .map(uid -> Notification.builder()
                        .schoolId(schoolId)
                        .userId(uid)
                        .targetRole(roleStr)
                        .title(title)
                        .message(message)
                        .notificationType(notificationType)
                        .category(cat)
                        .priority(prio)
                        .isRead(false)
                        .actorId(actor.actorId())
                        .actorRole(actor.actorRole())
                        .actorName(actor.actorName())
                        .actionUrl(actionUrl)
                        .actionType(actionType != null ? actionType : "NAVIGATE")
                        .entityType(entityType)
                        .entityId(entityId)
                        .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                        .build())
                .collect(Collectors.toList());

        List<Notification> saved = notificationRepository.saveAll(toSave);
        log.info("Broadcasted notification [{}] to {} users in role [{}] for school [{}]",
                notificationType, saved.size(), roleStr, schoolId);
        return saved;
    }

    @Override
    @Transactional
    public void notifyStudentAndParents(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String studentTitle,
            String studentMessage,
            String parentTitle,
            String parentMessage,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null || student == null) {
            return;
        }
        try {
            ActorContext actor = resolveActorContext(null, null, null);
            NotificationCategory category = resolveCategoryFromType(notificationType);
            NotificationPriority prio = priority != null ? priority : NotificationPriority.NORMAL;
            List<Notification> toSave = new ArrayList<>();

            // 1. Notify Student if userId is linked
            if (student.getUserId() != null && studentTitle != null && studentMessage != null) {
                toSave.add(Notification.builder()
                        .schoolId(schoolId)
                        .userId(student.getUserId())
                        .targetRole("STUDENT")
                        .title(studentTitle)
                        .message(studentMessage)
                        .notificationType(notificationType)
                        .category(category)
                        .priority(prio)
                        .isRead(false)
                        .actorId(actor.actorId())
                        .actorRole(actor.actorRole())
                        .actorName(actor.actorName())
                        .actionUrl(actionUrl)
                        .actionType("NAVIGATE")
                        .entityType(entityType)
                        .entityId(entityId)
                        .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                        .build());
            }

            // 2. Notify Parents linked to Student
            if (parentTitle != null && parentMessage != null) {
                Set<Long> parentUserIds = resolveParentUserIdsForStudent(student);
                for (Long parentUserId : parentUserIds) {
                    toSave.add(Notification.builder()
                            .schoolId(schoolId)
                            .userId(parentUserId)
                            .targetRole("PARENT")
                            .title(parentTitle)
                            .message(parentMessage)
                            .notificationType(notificationType)
                            .category(category)
                            .priority(prio)
                            .isRead(false)
                            .actorId(actor.actorId())
                            .actorRole(actor.actorRole())
                            .actorName(actor.actorName())
                            .actionUrl(actionUrl)
                            .actionType("NAVIGATE")
                            .entityType(entityType)
                            .entityId(entityId)
                            .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                            .build());
                }
            }

            if (!toSave.isEmpty()) {
                notificationRepository.saveAll(toSave);
            }
        } catch (Exception e) {
            log.warn("Failed to notify student/parents for studentId={}: {}", student.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifyClassAndSection(
            Long schoolId,
            Long classId,
            Long sectionId,
            boolean includeStudents,
            boolean includeParents,
            boolean includeTeachers,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null || classId == null) {
            return;
        }
        try {
            ActorContext actor = resolveActorContext(null, null, null);
            NotificationCategory category = resolveCategoryFromType(notificationType);
            NotificationPriority prio = priority != null ? priority : NotificationPriority.NORMAL;

            List<Student> students = (sectionId != null)
                    ? studentRepository.findBySchoolIdAndSchoolClassIdAndSectionId(schoolId, classId, sectionId)
                    : studentRepository.findBySchoolIdAndSchoolClassId(schoolId, classId);

            List<Notification> toSave = new ArrayList<>();

            if (includeStudents) {
                Set<Long> studentUserIds = students.stream()
                        .map(Student::getUserId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                for (Long uid : studentUserIds) {
                    toSave.add(buildNotification(schoolId, uid, "STUDENT", notificationType, category, prio,
                            title, message, actor, actionUrl, entityType, entityId, metadata));
                }
            }

            if (includeParents && !students.isEmpty()) {
                List<Long> studentIds = students.stream().map(Student::getId).filter(Objects::nonNull).toList();
                Set<Long> parentUserIds = new LinkedHashSet<>();
                if (!studentIds.isEmpty()) {
                    studentParentRepository.findByIdStudentIdIn(studentIds).forEach(sp -> {
                        if (sp.getParentUser() != null && sp.getParentUser().getId() != null) {
                            parentUserIds.add(sp.getParentUser().getId());
                        }
                    });
                }
                if (parentUserIds.isEmpty()) {
                    for (Student s : students) {
                        parentUserIds.addAll(resolveParentUserIdsForStudent(s));
                    }
                }
                for (Long pid : parentUserIds) {
                    toSave.add(buildNotification(schoolId, pid, "PARENT", notificationType, category, prio,
                            title, message, actor, actionUrl, entityType, entityId, metadata));
                }
            }

            if (includeTeachers) {
                Set<Long> teacherUserIds = resolveClassTeacherUserIds(schoolId, classId, sectionId);
                for (Long tid : teacherUserIds) {
                    toSave.add(buildNotification(schoolId, tid, "TEACHER", notificationType, category, prio,
                            title, message, actor, actionUrl, entityType, entityId, metadata));
                }
            }

            if (!toSave.isEmpty()) {
                notificationRepository.saveAll(toSave);
            }
        } catch (Exception e) {
            log.warn("Failed to notify class/section classId={} sectionId={}: {}", classId, sectionId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifyClassTeachersOfStudent(
            Long schoolId,
            Student student,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null || student == null || student.getSchoolClass() == null) {
            return;
        }
        try {
            Long classId = student.getSchoolClass().getId();
            Long sectionId = student.getSectionId();
            Set<Long> teacherUserIds = resolveClassTeacherUserIds(schoolId, classId, sectionId);
            if (teacherUserIds.isEmpty()) {
                return;
            }
            ActorContext actor = resolveActorContext(null, null, null);
            NotificationCategory category = resolveCategoryFromType(notificationType);
            NotificationPriority prio = priority != null ? priority : NotificationPriority.NORMAL;

            List<Notification> toSave = teacherUserIds.stream()
                    .map(tid -> buildNotification(schoolId, tid, "TEACHER", notificationType, category, prio,
                            title, message, actor, actionUrl, entityType, entityId, metadata))
                    .toList();
            notificationRepository.saveAll(toSave);
        } catch (Exception e) {
            log.warn("Failed to notify class teachers for studentId={}: {}", student.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifyStaffMember(
            Long schoolId,
            Staff staff,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null || staff == null || staff.getUserId() == null) {
            return;
        }
        try {
            String resolvedRole = resolveStaffRole(schoolId, staff);
            sendDirectNotification(
                    schoolId,
                    staff.getUserId(),
                    resolvedRole,
                    notificationType,
                    resolveCategoryFromType(notificationType),
                    priority != null ? priority : NotificationPriority.NORMAL,
                    title,
                    message,
                    null,
                    null,
                    null,
                    actionUrl,
                    "NAVIGATE",
                    entityType,
                    entityId,
                    metadata
            );
        } catch (Exception e) {
            log.warn("Failed to notify staffId={}: {}", staff.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void notifySchoolAdmins(
            Long schoolId,
            NotificationType notificationType,
            NotificationPriority priority,
            String title,
            String message,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        if (schoolId == null) {
            return;
        }
        try {
            broadcastToRole(
                    schoolId,
                    "ADMIN",
                    notificationType,
                    resolveCategoryFromType(notificationType),
                    priority != null ? priority : NotificationPriority.NORMAL,
                    title,
                    message,
                    null,
                    null,
                    null,
                    actionUrl,
                    "NAVIGATE",
                    entityType,
                    entityId,
                    metadata
            );
        } catch (Exception e) {
            log.warn("Failed to broadcast notification [{}] to admins in schoolId={}: {}", notificationType, schoolId, e.getMessage());
        }
    }

    private Set<Long> resolveParentUserIdsForStudent(Student student) {
        Set<Long> parentUserIds = new LinkedHashSet<>();
        if (student == null || student.getId() == null) {
            return parentUserIds;
        }
        List<StudentParent> studentParents = studentParentRepository.findByIdStudentId(student.getId());
        for (StudentParent sp : studentParents) {
            if (sp.getParentUser() != null && sp.getParentUser().getId() != null) {
                parentUserIds.add(sp.getParentUser().getId());
            } else if (sp.getId() != null && sp.getId().getParentUserId() != null) {
                parentUserIds.add(sp.getId().getParentUserId());
            }
        }
        if (parentUserIds.isEmpty()) {
            // Fallback lookup by guardian/father/mother phone or email
            for (String phone : Arrays.asList(student.getGuardianPhone(), student.getFatherPhone(), student.getMotherPhone())) {
                if (phone != null && !phone.isBlank()) {
                    userRepository.findByPhone(phone.trim()).ifPresent(u -> parentUserIds.add(u.getId()));
                }
            }
            for (String email : Arrays.asList(student.getGuardianEmail(), student.getFatherEmail(), student.getMotherEmail())) {
                if (email != null && !email.isBlank()) {
                    userRepository.findByEmail(email.trim()).ifPresent(u -> parentUserIds.add(u.getId()));
                }
            }
        }
        return parentUserIds;
    }

    private Set<Long> resolveClassTeacherUserIds(Long schoolId, Long classId, Long sectionId) {
        Set<Long> teacherUserIds = new LinkedHashSet<>();
        if (schoolId == null || classId == null) {
            return teacherUserIds;
        }
        List<ClassTeacherAssignment> ctaList = classTeacherAssignmentRepository.findBySchoolIdAndStatus(schoolId, "ACTIVE");
        for (ClassTeacherAssignment cta : ctaList) {
            if (cta.getSchoolClass() != null && classId.equals(cta.getSchoolClass().getId())) {
                boolean sectionMatches = sectionId == null
                        || cta.getSection() == null
                        || sectionId.equals(cta.getSection().getId());
                if (sectionMatches && cta.getStaff() != null && cta.getStaff().getUserId() != null) {
                    teacherUserIds.add(cta.getStaff().getUserId());
                }
            }
        }
        return teacherUserIds;
    }

    private String resolveStaffRole(Long schoolId, Staff staff) {
        if (staff.getUserId() != null) {
            List<UserSchoolRole> roles = userSchoolRoleRepository.findByUserIdAndSchoolId(staff.getUserId(), schoolId);
            for (UserSchoolRole usr : roles) {
                if ("ACTIVE".equalsIgnoreCase(usr.getStatus()) && usr.getRole() != null) {
                    return usr.getRole().name();
                }
            }
        }
        if (staff.getDepartment() != null && staff.getDepartment().toLowerCase().contains("teach")) {
            return "TEACHER";
        }
        if (staff.getDesignation() != null && staff.getDesignation().toLowerCase().contains("teach")) {
            return "TEACHER";
        }
        return "TEACHER";
    }

    private Notification buildNotification(
            Long schoolId,
            Long userId,
            String targetRole,
            NotificationType type,
            NotificationCategory category,
            NotificationPriority priority,
            String title,
            String message,
            ActorContext actor,
            String actionUrl,
            String entityType,
            Long entityId,
            Map<String, Object> metadata
    ) {
        return Notification.builder()
                .schoolId(schoolId)
                .userId(userId)
                .targetRole(targetRole)
                .title(title)
                .message(message)
                .notificationType(type)
                .category(category)
                .priority(priority)
                .isRead(false)
                .actorId(actor.actorId())
                .actorRole(actor.actorRole())
                .actorName(actor.actorName())
                .actionUrl(actionUrl)
                .actionType("NAVIGATE")
                .entityType(entityType)
                .entityId(entityId)
                .metadata(metadata != null ? new HashMap<>(metadata) : new HashMap<>())
                .build();
    }

    private record ActorContext(Long actorId, String actorRole, String actorName) {}

    private ActorContext resolveActorContext(Long explicitActorId, String explicitActorRole, String explicitActorName) {
        Long actorId = explicitActorId;
        String actorRole = explicitActorRole;
        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (actorId == null && currentUser != null) {
            actorId = currentUser.userId();
        }
        if ((actorRole == null || actorRole.isBlank()) && currentUser != null && currentUser.role() != null) {
            actorRole = currentUser.role().name();
        }
        if (actorRole == null || actorRole.isBlank()) {
            actorRole = "SYSTEM";
        }
        String actorName = (explicitActorName != null && !explicitActorName.isBlank())
                ? explicitActorName
                : resolveActorName(actorId, actorRole);
        return new ActorContext(actorId, actorRole, actorName);
    }

    private NotificationCategory resolveCategoryFromType(NotificationType type) {
        if (type == null) {
            return NotificationCategory.GENERAL;
        }
        return switch (type) {
            case HOMEWORK_ASSIGNED, HOMEWORK_SUBMITTED, HOMEWORK_GRADED,
                 EXAM_SCHEDULE_PUBLISHED, EXAM_ADMIT_CARD_READY, EXAM_RESULT_PUBLISHED,
                 EXAM_MARKS_ENTERED, TEACHER_REMARK_ADDED -> NotificationCategory.ACADEMIC;
            case ATTENDANCE_ABSENT, ATTENDANCE_LATE, ATTENDANCE_LOW_WARNING -> NotificationCategory.ATTENDANCE;
            case FEE_INVOICE_GENERATED, FEE_DUE_REMINDER, FEE_PAYMENT_SUCCESS,
                 FEE_OVERDUE_ALERT, FEE_DISCOUNT_APPLIED -> NotificationCategory.FINANCE;
            case LEAVE_REQUESTED, LEAVE_STATUS_UPDATED, PAYROLL_GENERATED,
                 PAYROLL_PAID, STAFF_TASK_ASSIGNED, STAFF_TASK_UPDATED -> NotificationCategory.HR;
            case STUDENT_LEAVE_REQUESTED, STUDENT_LEAVE_STATUS_UPDATED -> NotificationCategory.ATTENDANCE;
            case ONLINE_ADMISSION_SUBMITTED, ONLINE_ADMISSION_STATUS_UPDATED,
                 ADMISSION_ENQUIRY_CREATED, VISITOR_CHECKED_IN, GATE_PASS_ISSUED,
                 PARCEL_RECEIVED, PARCEL_COLLECTED, COMPLAINT_FILED, COMPLAINT_RESOLVED -> NotificationCategory.FRONTOFFICE;
            case BOOK_ISSUED, BOOK_RETURNED, BOOK_DUE_REMINDER, BOOK_OVERDUE_ALERT -> NotificationCategory.LIBRARY;
            case CIRCULAR_ANNOUNCEMENT, EMERGENCY_ALERT -> NotificationCategory.COMMUNICATION;
            case SUPPORT_TICKET_CREATED, SUPPORT_TICKET_UPDATED, SYSTEM_NOTIFICATION -> NotificationCategory.SYSTEM;
        };
    }

    private String resolveActorName(Long actorId, String defaultRole) {
        if (actorId == null) {
            return "System Notification";
        }
        return userRepository.findById(actorId)
                .map(User::getName)
                .filter(name -> name != null && !name.isBlank())
                .orElse(defaultRole);
    }
}
