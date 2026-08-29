-- Insert some fee categories if they don't exist
INSERT INTO fee_category (school_id, name, description) 
VALUES (4, 'Tuition Fee', 'Monthly Tuition Fee')
ON CONFLICT DO NOTHING;

INSERT INTO fee_category (school_id, name, description) 
VALUES (4, 'Transport Fee', 'Monthly Transport Fee')
ON CONFLICT DO NOTHING;

-- Generate sample dues for student 5 (and school 4, which is the mock school)
DO $$ 
DECLARE 
    v_tuition_cat_id BIGINT;
    v_transport_cat_id BIGINT;
BEGIN
    SELECT id INTO v_tuition_cat_id FROM fee_category WHERE school_id = 4 AND name = 'Tuition Fee' LIMIT 1;
    IF v_tuition_cat_id IS NULL THEN
        -- Fallback to first available category for school 4
        SELECT id INTO v_tuition_cat_id FROM fee_category WHERE school_id = 4 LIMIT 1;
    END IF;

    SELECT id INTO v_transport_cat_id FROM fee_category WHERE school_id = 4 AND name = 'Transport Fee' LIMIT 1;
    IF v_transport_cat_id IS NULL THEN
        v_transport_cat_id := v_tuition_cat_id;
    END IF;

    -- Insert term 1 dues
    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'April 2026 Tuition', 5000.00, '2026-04-10', 'UNPAID', 'Term 1');

    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_transport_cat_id, 'April 2026 Transport', 1500.00, '2026-04-10', 'UNPAID', 'Term 1');

    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'May 2026 Tuition', 5000.00, '2026-05-10', 'UNPAID', 'Term 1');

    -- Insert term 2 dues
    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'June 2026 Tuition', 5000.00, '2026-06-10', 'UNPAID', 'Term 2');
    
    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'July 2026 Tuition', 5000.00, '2026-07-10', 'UNPAID', 'Term 2');
    
    -- Insert term 3 dues
    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'August 2026 Tuition', 5000.00, '2026-08-10', 'UNPAID', 'Term 3');

    -- Insert some partially paid / paid dues for testing
    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, paid_amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'March 2026 Tuition', 5000.00, 5000.00, '2026-03-10', 'PAID', 'Previous Term');

    INSERT INTO fee_due (student_id, school_id, fee_category_id, title, amount, paid_amount, due_date, status, term_name)
    VALUES (5, 4, v_tuition_cat_id, 'February 2026 Tuition', 5000.00, 2000.00, '2026-02-10', 'PARTIAL', 'Previous Term');
END $$;
