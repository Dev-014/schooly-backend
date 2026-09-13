package com.school.erp.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttendanceRecordRequest {

    private Long id;

    @NotNull(message = "Student ID is required")
    private Long studentId;

    private Long termId;
    private Long classId;
    private Long sectionId;
    private Long examSetupId;
    private Long examScheduleId;

    private String roomNumber;

    @NotBlank(message = "Seat assignment is required")
    private String seatAssignment;

    @NotBlank(message = "Attendance status is required")
    private String attendanceStatus; // PRESENT, ABSENT, LEAVE

    private String remarks;
}
