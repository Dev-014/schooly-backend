DO $$
DECLARE
    rec RECORD;
    v_user_id BIGINT;
    v_role_exists BOOLEAN;
    v_student RECORD;
    v_link_exists BOOLEAN;
    v_default_hash TEXT := '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2';
    v_normalized_phone TEXT;
BEGIN
    -- Normalize existing student phones in School 4 (trim spaces and strip leading 0)
    UPDATE student 
    SET guardian_phone = LTRIM(TRIM(guardian_phone), '0')
    WHERE school_id = 4 AND guardian_phone IS NOT NULL AND guardian_phone != '';

    -- Loop through distinct parent phones in School 4
    FOR rec IN 
        SELECT DISTINCT guardian_phone, MAX(guardian_name) as p_name, MAX(guardian_email) as p_email
        FROM student 
        WHERE school_id = 4 AND guardian_phone IS NOT NULL AND guardian_phone != ''
        GROUP BY guardian_phone
    LOOP
        v_normalized_phone := rec.guardian_phone;
        
        -- 1. Create or Find User
        SELECT id INTO v_user_id FROM users WHERE phone = v_normalized_phone LIMIT 1;
        
        IF v_user_id IS NULL THEN
            INSERT INTO users (phone, name, email, password_hash, status, created_at, updated_at)
            VALUES (
                v_normalized_phone, 
                COALESCE(rec.p_name, 'Parent ' || v_normalized_phone), 
                rec.p_email, 
                v_default_hash, 
                'ACTIVE', 
                CURRENT_TIMESTAMP, 
                CURRENT_TIMESTAMP
            ) RETURNING id INTO v_user_id;
        END IF;

        -- 2. Assign Role if not present
        SELECT EXISTS (
            SELECT 1 FROM user_school_roles 
            WHERE user_id = v_user_id AND school_id = 4 AND role = 'PARENT'
        ) INTO v_role_exists;

        IF NOT v_role_exists THEN
            INSERT INTO user_school_roles (user_id, school_id, role, status)
            VALUES (v_user_id, 4, 'PARENT', 'ACTIVE');
        END IF;

        -- 3. Link Student and Parent
        FOR v_student IN 
            SELECT id, guardian_relation FROM student 
            WHERE school_id = 4 AND guardian_phone = v_normalized_phone
        LOOP
            SELECT EXISTS (
                SELECT 1 FROM student_parents 
                WHERE parent_user_id = v_user_id AND student_id = v_student.id
            ) INTO v_link_exists;

            IF NOT v_link_exists THEN
                INSERT INTO student_parents (student_id, parent_user_id, relation, is_primary)
                VALUES (
                    v_student.id, 
                    v_user_id, 
                    COALESCE(v_student.guardian_relation, 'Guardian'), 
                    true
                );
            END IF;
        END LOOP;
        
    END LOOP;
END $$;
