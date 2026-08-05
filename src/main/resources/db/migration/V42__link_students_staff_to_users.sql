-- For staff
DO $$
DECLARE
    staff_rec RECORD;
    new_user_id BIGINT;
    user_phone VARCHAR;
BEGIN
    FOR staff_rec IN SELECT * FROM staff WHERE user_id IS NULL LOOP
        user_phone := COALESCE(staff_rec.phone, 'STAFF' || staff_rec.id);
        
        -- Insert into users
        INSERT INTO users (phone, name, email, status)
        VALUES (user_phone, staff_rec.first_name || ' ' || staff_rec.last_name, staff_rec.email, 'ACTIVE')
        ON CONFLICT (phone) DO UPDATE SET name = EXCLUDED.name
        RETURNING id INTO new_user_id;
        
        -- Link staff to user
        UPDATE staff SET user_id = new_user_id WHERE id = staff_rec.id;
        
        -- Add user_school_role
        INSERT INTO user_school_roles (user_id, school_id, role, status)
        VALUES (new_user_id, staff_rec.school_id, 'TEACHER', 'ACTIVE')
        ON CONFLICT (user_id, school_id, role) DO NOTHING;
    END LOOP;
END $$;

-- For students
DO $$
DECLARE
    student_rec RECORD;
    new_user_id BIGINT;
    user_phone VARCHAR;
BEGIN
    FOR student_rec IN SELECT * FROM student WHERE user_id IS NULL LOOP
        user_phone := COALESCE(student_rec.guardian_phone, 'STU' || student_rec.id);
        
        -- Insert into users
        INSERT INTO users (phone, name, email, status)
        VALUES (user_phone, student_rec.first_name || ' ' || student_rec.last_name, student_rec.guardian_email, 'ACTIVE')
        ON CONFLICT (phone) DO UPDATE SET name = EXCLUDED.name
        RETURNING id INTO new_user_id;
        
        -- Link student to user
        UPDATE student SET user_id = new_user_id WHERE id = student_rec.id;
        
        -- Add user_school_role
        INSERT INTO user_school_roles (user_id, school_id, role, status)
        VALUES (new_user_id, student_rec.school_id, 'STUDENT', 'ACTIVE')
        ON CONFLICT (user_id, school_id, role) DO NOTHING;
    END LOOP;
END $$;
