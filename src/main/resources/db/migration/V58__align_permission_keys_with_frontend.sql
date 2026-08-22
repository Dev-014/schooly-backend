-- V58__align_permission_keys_with_frontend.sql
-- Aligns backend permission keys with frontend single source of truth

UPDATE permission_definitions 
SET permission_key = 'fees.fee_invoice.view' 
WHERE permission_key = 'fees.fee_collection.view';

UPDATE permission_definitions 
SET permission_key = 'fees.fee_invoice.edit' 
WHERE permission_key = 'fees.fee_collection.edit';

UPDATE permission_definitions 
SET permission_key = 'front_office.enquiry.view' 
WHERE permission_key = 'frontoffice.visitor.view';

UPDATE permission_definitions 
SET permission_key = 'front_office.enquiry.edit' 
WHERE permission_key = 'frontoffice.visitor.edit';

UPDATE permission_definitions 
SET permission_key = 'staff.staff.view', module_key = 'STAFF'
WHERE permission_key = 'hr.staff.view';

UPDATE permission_definitions 
SET permission_key = 'staff.staff.edit', module_key = 'STAFF'
WHERE permission_key = 'hr.staff.edit';

UPDATE permission_definitions 
SET permission_key = 'communication.message.view' 
WHERE permission_key = 'communication.message.send';

UPDATE permission_definitions 
SET permission_key = 'lesson_module.plan.view', module_key = 'LESSON_MODULE'
WHERE permission_key = 'lesson.plan.view';

UPDATE permission_definitions 
SET permission_key = 'lesson_module.plan.edit', module_key = 'LESSON_MODULE'
WHERE permission_key = 'lesson.plan.edit';

UPDATE permission_definitions 
SET permission_key = 'academic.class_setup.view', module_key = 'ACADEMIC'
WHERE permission_key = 'academics.timetable.view';

UPDATE permission_definitions 
SET permission_key = 'academic.class_setup.edit', module_key = 'ACADEMIC'
WHERE permission_key = 'academics.timetable.edit';
