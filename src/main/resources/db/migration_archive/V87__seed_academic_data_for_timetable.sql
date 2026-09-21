-- Sync serial sequences with existing table IDs before insertion
SELECT setval(pg_get_serial_sequence('timetable_periods', 'id'), COALESCE(max(id), 1)) FROM timetable_periods;
SELECT setval(pg_get_serial_sequence('subjects', 'id'), COALESCE(max(id), 1)) FROM subjects;
SELECT setval(pg_get_serial_sequence('class', 'id'), COALESCE(max(id), 1)) FROM class;
SELECT setval(pg_get_serial_sequence('sections', 'id'), COALESCE(max(id), 1)) FROM sections;
SELECT setval(pg_get_serial_sequence('academic_years', 'id'), COALESCE(max(id), 1)) FROM academic_years;
-- Seed Timetable Periods for School 1
INSERT INTO timetable_periods (school_id, period_number, name, start_time, end_time, is_break)
SELECT 1, 1, 'Period 1', '08:00', '08:45', false
WHERE NOT EXISTS (SELECT 1 FROM timetable_periods WHERE school_id = 1 AND period_number = 1);

INSERT INTO timetable_periods (school_id, period_number, name, start_time, end_time, is_break)
SELECT 1, 2, 'Period 2', '08:45', '09:30', false
WHERE NOT EXISTS (SELECT 1 FROM timetable_periods WHERE school_id = 1 AND period_number = 2);

INSERT INTO timetable_periods (school_id, period_number, name, start_time, end_time, is_break)
SELECT 1, 3, 'Morning Break', '09:30', '09:45', true
WHERE NOT EXISTS (SELECT 1 FROM timetable_periods WHERE school_id = 1 AND period_number = 3);

INSERT INTO timetable_periods (school_id, period_number, name, start_time, end_time, is_break)
SELECT 1, 4, 'Period 3', '09:45', '10:30', false
WHERE NOT EXISTS (SELECT 1 FROM timetable_periods WHERE school_id = 1 AND period_number = 4);

-- Seed Subjects for School 1
INSERT INTO subjects (school_id, code, name, type, credits)
SELECT 1, 'MATH101', 'Mathematics', 'THEORY', 4
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = 1 AND code = 'MATH101');

INSERT INTO subjects (school_id, code, name, type, credits)
SELECT 1, 'PHY101', 'Physics', 'THEORY', 4
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = 1 AND code = 'PHY101');

INSERT INTO subjects (school_id, code, name, type, credits)
SELECT 1, 'CHEM101', 'Chemistry', 'THEORY', 4
WHERE NOT EXISTS (SELECT 1 FROM subjects WHERE school_id = 1 AND code = 'CHEM101');

-- Ensure School Classes exist
INSERT INTO class (school_id, name)
SELECT 1, 'Class 10'
WHERE NOT EXISTS (SELECT 1 FROM class WHERE school_id = 1 AND name = 'Class 10');

INSERT INTO class (school_id, name)
SELECT 1, 'Class 11'
WHERE NOT EXISTS (SELECT 1 FROM class WHERE school_id = 1 AND name = 'Class 11');

-- Ensure Sections exist
INSERT INTO sections (school_id, class_id, name, capacity)
SELECT 1, (SELECT id FROM class WHERE school_id = 1 AND name = 'Class 10' LIMIT 1), 'Section A', 40
WHERE NOT EXISTS (SELECT 1 FROM sections WHERE school_id = 1 AND name = 'Section A' AND class_id = (SELECT id FROM class WHERE school_id = 1 AND name = 'Class 10' LIMIT 1));

-- Ensure Academic Years exist
INSERT INTO academic_years (school_id, name, display_name, start_date, end_date, status)
SELECT 1, '2026-2027', 'AY 2026-2027', '2026-04-01', '2027-03-31', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM academic_years WHERE school_id = 1 AND name = '2026-2027');

