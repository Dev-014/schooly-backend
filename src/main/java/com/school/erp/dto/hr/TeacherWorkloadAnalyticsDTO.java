package com.school.erp.dto.hr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherWorkloadAnalyticsDTO {
    private Long schoolId;
    private Long academicYearId;
    private Integer totalTeachers;
    private Integer activeTeachers;
    private Integer absentTeachersCount;
    private Double averageUtilization;
    private Integer totalAssignedLectures;
    private List<TeacherWorkloadItem> teachers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherWorkloadItem {
        private Long id;
        private String name;
        private String dept;
        private String avatar;
        private Integer lectures;
        private Integer free;
        private Integer utilization;
        private Boolean absent;
        private String variant;
        private List<String> gap;
        private List<List<Integer>> schedule;
    }
}
