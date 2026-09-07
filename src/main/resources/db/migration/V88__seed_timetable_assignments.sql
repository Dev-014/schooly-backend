-- Seed Timetable Assignments for School 1, Class 10, Section A, MONDAY and TUESDAY

-- Ensure we have a valid academic year, class, section, teacher
DO $$ 
DECLARE
    v_school_id BIGINT := 1;
    v_class_id BIGINT;
    v_section_id BIGINT;
    v_year_id BIGINT;
    v_teacher_id BIGINT;
    v_subject_math BIGINT;
    v_subject_phy BIGINT;
    v_subject_chem BIGINT;
BEGIN
    SELECT id INTO v_class_id FROM class WHERE school_id = v_school_id AND name = 'Class 10' LIMIT 1;
    SELECT id INTO v_section_id FROM sections WHERE school_id = v_school_id AND name = 'Section A' AND class_id = v_class_id LIMIT 1;
    SELECT id INTO v_year_id FROM academic_years WHERE school_id = v_school_id AND status = 'ACTIVE' LIMIT 1;
    SELECT id INTO v_teacher_id FROM staff WHERE school_id = v_school_id LIMIT 1;
    
    SELECT id INTO v_subject_math FROM subjects WHERE school_id = v_school_id AND code = 'MATH101' LIMIT 1;
    SELECT id INTO v_subject_phy FROM subjects WHERE school_id = v_school_id AND code = 'PHY101' LIMIT 1;
    SELECT id INTO v_subject_chem FROM subjects WHERE school_id = v_school_id AND code = 'CHEM101' LIMIT 1;

    IF v_class_id IS NOT NULL AND v_section_id IS NOT NULL AND v_year_id IS NOT NULL AND v_teacher_id IS NOT NULL THEN
        -- MONDAY
        -- Period 1 (Math)
        INSERT INTO timetable_entries (school_id, class_id, section_id, academic_year_id, day_of_week, period_id, subject_id, teacher_id, room_number)
        SELECT v_school_id, v_class_id, v_section_id, v_year_id, 'MONDAY', (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 1), v_subject_math, v_teacher_id, 'Room 101'
        WHERE NOT EXISTS (SELECT 1 FROM timetable_entries WHERE school_id = v_school_id AND class_id = v_class_id AND section_id = v_section_id AND day_of_week = 'MONDAY' AND period_id = (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 1));

        -- Period 2 (Physics)
        INSERT INTO timetable_entries (school_id, class_id, section_id, academic_year_id, day_of_week, period_id, subject_id, teacher_id, room_number)
        SELECT v_school_id, v_class_id, v_section_id, v_year_id, 'MONDAY', (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 2), v_subject_phy, v_teacher_id, 'Lab 1'
        WHERE NOT EXISTS (SELECT 1 FROM timetable_entries WHERE school_id = v_school_id AND class_id = v_class_id AND section_id = v_section_id AND day_of_week = 'MONDAY' AND period_id = (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 2));

        -- Period 4 (Chemistry)
        INSERT INTO timetable_entries (school_id, class_id, section_id, academic_year_id, day_of_week, period_id, subject_id, teacher_id, room_number)
        SELECT v_school_id, v_class_id, v_section_id, v_year_id, 'MONDAY', (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 4), v_subject_chem, v_teacher_id, 'Lab 2'
        WHERE NOT EXISTS (SELECT 1 FROM timetable_entries WHERE school_id = v_school_id AND class_id = v_class_id AND section_id = v_section_id AND day_of_week = 'MONDAY' AND period_id = (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 4));

        -- TUESDAY
        -- Period 1 (Physics)
        INSERT INTO timetable_entries (school_id, class_id, section_id, academic_year_id, day_of_week, period_id, subject_id, teacher_id, room_number)
        SELECT v_school_id, v_class_id, v_section_id, v_year_id, 'TUESDAY', (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 1), v_subject_phy, v_teacher_id, 'Lab 1'
        WHERE NOT EXISTS (SELECT 1 FROM timetable_entries WHERE school_id = v_school_id AND class_id = v_class_id AND section_id = v_section_id AND day_of_week = 'TUESDAY' AND period_id = (SELECT id FROM timetable_periods WHERE school_id = v_school_id AND period_number = 1));
        
    END IF;
END $$;
