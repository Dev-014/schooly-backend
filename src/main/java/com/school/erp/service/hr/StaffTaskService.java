package com.school.erp.service.hr;

import com.school.erp.dto.hr.StaffTaskDTO;
import com.school.erp.dto.hr.StaffTaskRequest;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffTask;
import com.school.erp.entity.notification.NotificationPriority;
import com.school.erp.entity.notification.NotificationType;
import com.school.erp.entity.superadmin.School;
import com.school.erp.repository.hr.StaffRepository;
import com.school.erp.repository.hr.StaffTaskRepository;
import com.school.erp.repository.superadmin.SchoolRepository;
import com.school.erp.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffTaskService {

    private final StaffTaskRepository taskRepository;
    private final StaffRepository staffRepository;
    private final SchoolRepository schoolRepository;

    @Autowired(required = false)
    private NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<StaffTaskDTO> getTasks(Long schoolId, Long staffId, String status, String priority) {
        List<StaffTask> tasks = (staffId != null)
                ? taskRepository.findByStaffId(staffId)
                : taskRepository.findBySchoolId(schoolId);

        if (status != null && !status.trim().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase(status.trim()))
                    .collect(Collectors.toList());
        }

        if (priority != null && !priority.trim().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> t.getPriority() != null && t.getPriority().equalsIgnoreCase(priority.trim()))
                    .collect(Collectors.toList());
        }

        return tasks.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StaffTaskDTO getTaskById(Long schoolId, Long id) {
        StaffTask task = taskRepository.findById(id)
                .filter(t -> t.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return mapToDTO(task);
    }

    @Transactional
    public StaffTaskDTO createTask(Long schoolId, StaffTaskRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        Staff staff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                .orElseThrow(() -> new RuntimeException("Staff member not found"));

        StaffTask task = new StaffTask();
        task.setSchool(school);
        task.setStaff(staff);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM");
        task.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "PENDING");

        StaffTask saved = taskRepository.save(task);

        if (notificationService != null) {
            NotificationPriority priority = "HIGH".equalsIgnoreCase(saved.getPriority()) || "URGENT".equalsIgnoreCase(saved.getPriority())
                    ? NotificationPriority.HIGH
                    : NotificationPriority.MEDIUM;
            notificationService.notifyStaffMember(
                    schoolId,
                    staff,
                    NotificationType.STAFF_TASK_ASSIGNED,
                    priority,
                    "New Task Assigned: " + saved.getTitle(),
                    "You have been assigned a new task: \"" + saved.getTitle() + "\""
                            + (saved.getDueDate() != null ? " (Due: " + saved.getDueDate() + ")." : "."),
                    "/hr/tasks",
                    "View Task",
                    "STAFF_TASK",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "taskId", saved.getId(),
                            "title", saved.getTitle(),
                            "priority", saved.getPriority()
                    )
            );
        }

        return mapToDTO(saved);
    }

    @Transactional
    public StaffTaskDTO updateTask(Long schoolId, Long id, StaffTaskRequest request) {
        StaffTask task = taskRepository.findById(id)
                .filter(t -> t.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.getStaffId() != null && !request.getStaffId().equals(task.getStaff().getId())) {
            Staff newStaff = staffRepository.findByIdAndSchoolId(request.getStaffId(), schoolId)
                    .orElseThrow(() -> new RuntimeException("Staff member not found"));
            task.setStaff(newStaff);
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority().toUpperCase());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus().toUpperCase());
        }

        return mapToDTO(taskRepository.save(task));
    }

    @Transactional
    public StaffTaskDTO updateTaskStatus(Long schoolId, Long id, String status) {
        StaffTask task = taskRepository.findById(id)
                .filter(t -> t.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus(status != null ? status.toUpperCase() : "PENDING");
        StaffTask saved = taskRepository.save(task);
        StaffTaskDTO dto = mapToDTO(saved);

        if (notificationService != null && "COMPLETED".equalsIgnoreCase(saved.getStatus())) {
            notificationService.notifySchoolAdmins(
                    schoolId,
                    NotificationType.STAFF_TASK_UPDATED,
                    NotificationPriority.LOW,
                    "Staff Task Completed: " + saved.getTitle(),
                    dto.getStaffName() + " marked task \"" + saved.getTitle() + "\" as COMPLETED.",
                    "/hr/tasks",
                    "View Task",
                    "STAFF_TASK",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "taskId", saved.getId(),
                            "title", saved.getTitle(),
                            "status", saved.getStatus(),
                            "staffName", dto.getStaffName()
                    )
            );
        }

        return dto;
    }

    @Transactional
    public void deleteTask(Long schoolId, Long id) {
        StaffTask task = taskRepository.findById(id)
                .filter(t -> t.getSchool().getId().equals(schoolId))
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }

    private StaffTaskDTO mapToDTO(StaffTask t) {
        String staffName = t.getStaff() != null
                ? ((t.getStaff().getFirstName() != null ? t.getStaff().getFirstName() : "") + " " +
                (t.getStaff().getLastName() != null ? t.getStaff().getLastName() : "")).trim()
                : "Unassigned";

        return StaffTaskDTO.builder()
                .id(t.getId())
                .schoolId(t.getSchool().getId())
                .staffId(t.getStaff() != null ? t.getStaff().getId() : null)
                .staffName(staffName)
                .title(t.getTitle())
                .description(t.getDescription())
                .startDate(t.getStartDate())
                .dueDate(t.getDueDate())
                .priority(t.getPriority())
                .status(t.getStatus())
                .build();
    }
}
