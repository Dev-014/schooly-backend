package com.school.erp.service.hr;

import com.school.erp.dto.hr.TeacherWorkloadAnalyticsDTO;
import com.school.erp.entity.academic.TimetableEntry;
import com.school.erp.entity.hr.Staff;
import com.school.erp.entity.hr.StaffAttendance;
import com.school.erp.repository.academic.TimetableEntryRepository;
import com.school.erp.repository.hr.StaffAttendanceRepository;
import com.school.erp.repository.hr.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherWorkloadService {

    private final TimetableEntryRepository timetableEntryRepository;
    private final StaffRepository staffRepository;
    private final StaffAttendanceRepository staffAttendanceRepository;

    @Transactional(readOnly = true)
    public TeacherWorkloadAnalyticsDTO getWorkloadAnalytics(Long schoolId, Long academicYearId, Long departmentId) {
        List<Staff> staffList = staffRepository.findBySchoolId(schoolId);

        if (departmentId != null) {
            staffList = staffList.stream()
                    .filter(s -> departmentId.equals(s.getDepartmentId()))
                    .collect(Collectors.toList());
        }

        // Fetch timetable entries
        List<TimetableEntry> entries = (academicYearId != null)
                ? timetableEntryRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId)
                : timetableEntryRepository.findBySchoolId(schoolId);

        // Group entries by teacher ID
        Map<Long, List<TimetableEntry>> entriesByTeacher = entries.stream()
                .filter(e -> e.getTeacher() != null && "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.groupingBy(e -> e.getTeacher().getId()));

        // Today's attendance for school
        LocalDate today = LocalDate.now();
        String currentDayOfWeek = today.getDayOfWeek().name(); // MONDAY, TUESDAY...
        List<StaffAttendance> todayAttendances = staffAttendanceRepository.findBySchoolIdAndAttendanceDate(schoolId, today);
        Map<Long, String> todayAttendanceStatusByStaff = todayAttendances.stream()
                .filter(a -> a.getStaff() != null && a.getStatus() != null)
                .collect(Collectors.toMap(a -> a.getStaff().getId(), StaffAttendance::getStatus, (s1, s2) -> s1));

        List<String> days = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY");

        int totalLecturesSum = 0;
        int totalUtilizationSum = 0;
        int absentCount = 0;

        List<TeacherWorkloadAnalyticsDTO.TeacherWorkloadItem> teacherItems = new ArrayList<>();

        for (int i = 0; i < staffList.size(); i++) {
            Staff s = staffList.get(i);
            List<TimetableEntry> teacherEntries = entriesByTeacher.getOrDefault(s.getId(), List.of());
            int lectures = teacherEntries.size();
            int free = Math.max(0, 30 - lectures);
            int utilization = Math.min(100, (int) Math.round((lectures / 30.0) * 100));

            totalLecturesSum += lectures;
            totalUtilizationSum += utilization;

            String attStatus = todayAttendanceStatusByStaff.get(s.getId());
            boolean isAbsent = "ON_LEAVE".equalsIgnoreCase(s.getStatus()) ||
                    "LEAVE".equalsIgnoreCase(s.getStatus()) ||
                    "ABSENT".equalsIgnoreCase(attStatus) ||
                    "ON_LEAVE".equalsIgnoreCase(attStatus);

            if (isAbsent) {
                absentCount++;
            }

            // AM and PM schedule breakdown
            List<Integer> amRow = new ArrayList<>();
            List<Integer> pmRow = new ArrayList<>();
            for (String day : days) {
                boolean hasAm = teacherEntries.stream().anyMatch(e -> day.equalsIgnoreCase(e.getDayOfWeek()) &&
                        e.getPeriod() != null && e.getPeriod().getPeriodNumber() != null && e.getPeriod().getPeriodNumber() <= 4);
                boolean hasPm = teacherEntries.stream().anyMatch(e -> day.equalsIgnoreCase(e.getDayOfWeek()) &&
                        e.getPeriod() != null && e.getPeriod().getPeriodNumber() != null && e.getPeriod().getPeriodNumber() > 4);
                amRow.add(hasAm ? 1 : 0);
                pmRow.add(hasPm ? 1 : 0);
            }
            List<List<Integer>> schedule = List.of(amRow, pmRow);

            // Absence gap alerts
            List<String> gap = null;
            if (isAbsent) {
                List<TimetableEntry> todayEntries = teacherEntries.stream()
                        .filter(e -> currentDayOfWeek.equalsIgnoreCase(e.getDayOfWeek()))
                        .collect(Collectors.toList());

                if (!todayEntries.isEmpty()) {
                    gap = todayEntries.stream().map(e -> {
                        String time = (e.getPeriod() != null && e.getPeriod().getStartTime() != null ? e.getPeriod().getStartTime().toString() : "") +
                                (e.getPeriod() != null && e.getPeriod().getEndTime() != null ? " - " + e.getPeriod().getEndTime().toString() : "");
                        String sub = e.getSubject() != null ? e.getSubject().getName() : "Subject";
                        String cls = e.getSchoolClass() != null ? e.getSchoolClass().getName() : "Class";
                        return (!time.isEmpty() ? time + " " : "") + "(" + sub + " in " + cls + ")";
                    }).collect(Collectors.toList());
                } else {
                    gap = List.of("No classes scheduled for today");
                }
            }

            String fullName = ((s.getFirstName() != null ? s.getFirstName() : "") + " " +
                    (s.getLastName() != null ? s.getLastName() : "")).trim();
            String avatar = s.getPhotoUrl() != null ? s.getPhotoUrl() :
                    "https://ui-avatars.com/api/?name=" + (fullName.isEmpty() ? "T" : fullName.replace(" ", "+"));

            teacherItems.add(TeacherWorkloadAnalyticsDTO.TeacherWorkloadItem.builder()
                    .id(s.getId())
                    .name(!fullName.isEmpty() ? fullName : "Teacher #" + s.getId())
                    .dept(s.getDepartment() != null ? s.getDepartment() : "General")
                    .avatar(avatar)
                    .lectures(lectures)
                    .free(free)
                    .utilization(utilization)
                    .absent(isAbsent)
                    .variant(i % 2 == 0 ? "tertiary" : "secondary")
                    .gap(gap)
                    .schedule(schedule)
                    .build());
        }

        int totalTeachers = staffList.size();
        double avgUtilization = totalTeachers > 0
                ? Math.round((totalUtilizationSum * 10.0) / totalTeachers) / 10.0
                : 0.0;

        return TeacherWorkloadAnalyticsDTO.builder()
                .schoolId(schoolId)
                .academicYearId(academicYearId)
                .totalTeachers(totalTeachers)
                .activeTeachers(Math.max(0, totalTeachers - absentCount))
                .absentTeachersCount(absentCount)
                .averageUtilization(avgUtilization)
                .totalAssignedLectures(totalLecturesSum)
                .teachers(teacherItems)
                .build();
    }
}
