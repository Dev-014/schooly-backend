package com.school.erp.repository;

import com.school.erp.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findBySchoolIdAndSchoolClassIdAndAcademicYearId(Long schoolId, Long classId, Long academicYearId);
    List<TimetableEntry> findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearId(Long schoolId, Long classId, Long sectionId, Long academicYearId);
    List<TimetableEntry> findBySchoolIdAndSchoolClassIdAndSectionIdAndDayOfWeekAndAcademicYearId(Long schoolId, Long classId, Long sectionId, String dayOfWeek, Long academicYearId);
    List<TimetableEntry> findBySchoolIdAndSchoolClassIdAndDayOfWeekAndAcademicYearId(Long schoolId, Long classId, String dayOfWeek, Long academicYearId);
    List<TimetableEntry> findBySchoolIdAndTeacherIdAndAcademicYearId(Long schoolId, Long teacherId, Long academicYearId);
    List<TimetableEntry> findBySchoolIdAndTeacherIdAndDayOfWeekAndAcademicYearId(Long schoolId, Long teacherId, String dayOfWeek, Long academicYearId);
    Optional<TimetableEntry> findBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearIdAndDayOfWeekAndPeriodId(Long schoolId, Long classId, Long sectionId, Long academicYearId, String dayOfWeek, Long periodId);
    void deleteBySchoolIdAndSchoolClassIdAndSectionIdAndAcademicYearId(Long schoolId, Long classId, Long sectionId, Long academicYearId);
}
