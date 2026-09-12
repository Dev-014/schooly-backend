-- Safely seed fee categories and sample dues for an existing school and student
DO $$ 
DECLARE 
    v_school_id BIGINT;
    v_student_id BIGINT;
    v_tuition_cat_id BIGINT;
    v_transport_cat_id BIGINT;
BEGIN
    -- Dynamically find an existing school
    SELECT id INTO v_school_id FROM schools ORDER BY id LIMIT 1;
    IF v_school_id IS NOT NULL THEN
        -- Insert fee categories for this school if they don't exist
        INSERT INTO fee_category (school_id, name, description) 
        VALUES (v_school_id, 'Tuition Fee', 'Monthly Tuition Fee')
        ON CONFLICT DO NOTHING;

        INSERT INTO fee_category (school_id, name, description) 
        VALUES (v_school_id, 'Transport Fee', 'Monthly Transport Fee')
        ON CONFLICT DO NOTHING;

        SELECT id INTO v_tuition_cat_id FROM fee_category WHERE school_id = v_school_id AND name = 'Tuition Fee' LIMIT 1;
        IF v_tuition_cat_id IS NULL THEN
            SELECT id INTO v_tuition_cat_id FROM fee_category WHERE school_id = v_school_id LIMIT 1;
        END IF;

        SELECT id INTO v_transport_cat_id FROM fee_category WHERE school_id = v_school_id AND name = 'Transport Fee' LIMIT 1;
        IF v_transport_cat_id IS NULL THEN
            v_transport_cat_id := v_tuition_cat_id;
        END IF;

        SELECT id INTO v_student_id FROM student WHERE school_id = v_school_id ORDER BY id LIMIT 1;
        IF v_student_id IS NULL THEN
            SELECT id INTO v_student_id FROM student ORDER BY id LIMIT 1;
        END IF;

        IF v_student_id IS NOT NULL AND v_tuition_cat_id IS NOT NULL THEN
            -- Insert term 1 dues
            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'April 2026 Tuition', 5000.00, '2026-04-10', 'UNPAID', 'Term 1');

            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_transport_cat_id, 'April 2026 Transport', 1500.00, '2026-04-10', 'UNPAID', 'Term 1');

            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'May 2026 Tuition', 5000.00, '2026-05-10', 'UNPAID', 'Term 1');

            -- Insert term 2 dues
            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'June 2026 Tuition', 5000.00, '2026-06-10', 'UNPAID', 'Term 2');
            
            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'July 2026 Tuition', 5000.00, '2026-07-10', 'UNPAID', 'Term 2');
            
            -- Insert term 3 dues
            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'August 2026 Tuition', 5000.00, '2026-08-10', 'UNPAID', 'Term 3');

            -- Insert some partially paid / paid dues for testing
            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, paid_amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'March 2026 Tuition', 5000.00, 5000.00, '2026-03-10', 'PAID', 'Previous Term');

            INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, paid_amount, due_date, status, term_name)
            VALUES (v_student_id, v_school_id, v_tuition_cat_id, 'February 2026 Tuition', 5000.00, 2000.00, '2026-02-10', 'PARTIAL', 'Previous Term');
        END IF;
    END IF;
END $$;
