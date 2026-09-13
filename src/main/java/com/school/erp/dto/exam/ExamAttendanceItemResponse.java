package com.school.erp.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttendanceItemResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String rollNo;
    private String studentIdCode;
    private String seatAssignment;
    private String attendanceStatus; // PRESENT, ABSENT, LEAVE
    private String initials;
    private String roomNumber;
    private String sessionStatus;
    private String remarks;
}
