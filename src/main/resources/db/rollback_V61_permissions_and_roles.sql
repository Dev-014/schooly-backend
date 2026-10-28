--
-- PostgreSQL database dump
--

\restrict PRFbZxF7X3WFjjUai9IFnrUuj2fnPQ48NoglMZrpYrccxjzv0Wod1oQEaDQ1hOb

-- Dumped from database version 18.6 (c021049)
-- Dumped by pg_dump version 18.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: permission_definitions; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, requires_assignment, is_sensitive, is_system_permission, is_active, created_at, audience) FROM stdin;
perm_attendance_edit	attendance.attendance_record.edit	ATTENDANCE	attendance_record	edit	Edit Attendance	Allows modification of student attendance records	["school", "assigned", "class", "section"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_fee_refund	fees.fee_collection.refund	FEES	fee_collection	refund	Refund Fees	Allows processing fee refunds	["school"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_exam_view	exams.marks.view	EXAMS_RESULTS	marks	view	View Exam Marks	Allows viewing student exam marks	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_exam_edit	exams.marks.edit	EXAMS_RESULTS	marks	edit	Enter Exam Marks	Allows entering marks for exams	["school", "class"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_homework_edit	homework.assignment.edit	HOMEWORK	assignment	edit	Assign Homework	Allows assigning homework	["school", "class"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_settings_edit	settings.academic.edit	SETTINGS	academic	edit	Manage Settings	Allows editing global school settings	["school"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_certificates_edit	certificates.certificate.edit	CERTIFICATES	certificate	edit	Manage Certificates	Allows generating and managing certificates and ID cards	["school"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_onlineclass_edit	online_class.session.edit	ONLINE_CLASS	session	edit	Manage Online Classes	Allows creating and starting online classes	["school", "class", "section"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_onlineexams_edit	online_exams.exam.edit	ONLINE_EXAMS	exam	edit	Manage Online Exams	Allows creating online exams and question banks	["school", "class"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_fee_edit	fees.fee_invoice.edit	FEES	fee_collection	edit	Collect Fees	Allows collecting fees	["school"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_hr_view	staff.staff.view	STAFF_HR	staff	view	View Staff	Allows viewing staff list	["school"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_hr_edit	staff.staff.edit	STAFF_HR	staff	edit	Manage Staff	Allows managing staff HR and payroll	["school"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff"]
perm_downloads_edit	downloads.material.edit	DOWNLOADS	material	edit	Manage Downloads	Allows uploading study materials	["school", "class"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_library_edit	library.book.edit	LIBRARY	book	edit	Manage Library	Allows adding books and issuing/returning them	["school"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_questionpaper_edit	question_paper.paper.edit	QUESTION_PAPER	paper	edit	Manage Question Papers	Allows generating question papers	["school", "class"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_discipline_edit	discipline.record.edit	DISCIPLINE	record	edit	Manage Discipline	Allows creating and managing disciplinary records	["school", "class"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_inventory_view	inventory.stock.view	INVENTORY	stock	view	View Inventory	Allows viewing inventory and stock	["school"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_inventory_edit	inventory.stock.edit	INVENTORY	stock	edit	Manage Inventory	Allows managing stock, items, and vendors	["school"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_lesson_edit	lesson_module.plan.edit	LESSON_MODULE	plan	edit	Manage Lesson Plans	Allows creating and managing lesson plans	["school", "class"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_staff_hr_staff_directory_view	staff_hr.staff_directory.view	STAFF_HR	staff_directory	view	Access Staff Directory	Allows access to the Staff Directory screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_staff_hr_teacher_workload_view	staff_hr.teacher_workload.view	STAFF_HR	teacher_workload	view	Access Teacher Workload	Allows access to the Teacher Workload screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_staff_hr_payroll_view	staff_hr.payroll.view	STAFF_HR	payroll	view	Access Payroll	Allows access to the Payroll screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_attendance_register_view	attendance.attendance_register.view	ATTENDANCE	attendance_register	view	Access Take Register	Allows access to the Take Register screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_attendance_analytics_view	attendance.attendance_analytics.view	ATTENDANCE	attendance_analytics	view	Access Analytics	Allows access to the Analytics screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_attendance_my_records_view	attendance.attendance_my_records.view	ATTENDANCE	attendance_my_records	view	Access My Attendance	Allows access to the My Attendance screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_view	attendance.attendance_record.view	ATTENDANCE	attendance_record	view	View Attendance	Allows viewing of student attendance records	["school", "assigned", "class", "section", "linked"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff", "student", "parent"]
perm_homework_view	homework.assignment.view	HOMEWORK	assignment	view	View Homework	Allows viewing homework assignments	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff", "student", "parent"]
perm_onlineexams_view	online_exams.exam.view	ONLINE_EXAMS	exam	view	View Online Exams	Allows viewing online exams and results	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_attendance_student_attendance_view	attendance.student_attendance.view	ATTENDANCE	student_attendance	view	Access Student Attendance	Allows access to the Student Attendance screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_attendance_reports_view	attendance.attendance_reports.view	ATTENDANCE	attendance_reports	view	Access Attendance Reports	Allows access to the Attendance Reports screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_view	academic.class_setup.view	ACADEMICS	timetable	view	View Academics	Allows viewing classes, subjects, and timetables	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_academics_edit	academic.class_setup.edit	ACADEMICS	timetable	edit	Manage Academics	Allows configuring classes, subjects, and timetables	["school"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_certificates_student_certificates_view	certificates.student_certificates.view	CERTIFICATES	student_certificates	view	Access Student Certificates	Allows access to the Student Certificates screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_certificates_staff_certificates_view	certificates.staff_certificates.view	CERTIFICATES	staff_certificates	view	Access Staff Certificates	Allows access to the Staff Certificates screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_certificates_student_id_cards_view	certificates.student_id_cards.view	CERTIFICATES	student_id_cards	view	Access Student ID Cards	Allows access to the Student ID Cards screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_certificates_staff_id_cards_view	certificates.staff_id_cards.view	CERTIFICATES	staff_id_cards	view	Access Staff ID Cards	Allows access to the Staff ID Cards screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_certificates_id_configuration_view	certificates.id_configuration.view	CERTIFICATES	id_configuration	view	Access Id Configuration	Allows access to the Id Configuration screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_class_online_class_management_view	online_class.online_class_management.view	ONLINE_CLASS	online_class_management	view	Access Online Class Management	Allows access to the Online Class Management screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_class_teacher_timetable_view	online_class.teacher_timetable.view	ONLINE_CLASS	teacher_timetable	view	Access Teacher Timetable	Allows access to the Teacher Timetable screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_class_attendance_detailed_view	online_class.attendance_detailed.view	ONLINE_CLASS	attendance_detailed	view	Access Attendance Detailed	Allows access to the Attendance Detailed screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_exams_create_exam_view	online_exams.create_exam.view	ONLINE_EXAMS	create_exam	view	Access Create Exam	Allows access to the Create Exam screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_exams_question_bank_view	online_exams.question_bank.view	ONLINE_EXAMS	question_bank	view	Access Question Bank	Allows access to the Question Bank screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_exams_exam_report_view	online_exams.exam_report.view	ONLINE_EXAMS	exam_report	view	Access Exam Report	Allows access to the Exam Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_online_exams_student_exam_report_view	online_exams.student_exam_report.view	ONLINE_EXAMS	student_exam_report	view	Access Student Exam Report	Allows access to the Student Exam Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_lesson_module_lesson_view	lesson_module.lesson.view	LESSON_MODULE	lesson	view	Access Lesson	Allows access to the Lesson screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_lesson_module_manage_planner_view	lesson_module.manage_planner.view	LESSON_MODULE	manage_planner	view	Access Manage Planner	Allows access to the Manage Planner screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_lesson_module_topic_view	lesson_module.topic.view	LESSON_MODULE	topic	view	Access Topic	Allows access to the Topic screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_lesson_module_topic_report_view	lesson_module.topic_report.view	LESSON_MODULE	topic_report	view	Access Topic Report	Allows access to the Topic Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_lesson_module_lesson_report_view	lesson_module.lesson_report.view	LESSON_MODULE	lesson_report	view	Access Lesson Report	Allows access to the Lesson Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_term_setup_view	exams_results.term_setup.view	EXAMS_RESULTS	term_setup	view	Access Term Setup	Allows access to the Term Setup screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_exam_setup_student_view	exams_results.exam_setup_student.view	EXAMS_RESULTS	exam_setup_student	view	Access Exam Setup Student	Allows access to the Exam Setup Student screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_exam_schedule_student_view	exams_results.exam_schedule_student.view	EXAMS_RESULTS	exam_schedule_student	view	Access Exam Schedule Student	Allows access to the Exam Schedule Student screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_admit_card_students_view	exams_results.admit_card_students.view	EXAMS_RESULTS	admit_card_students	view	Access Admit Card Students	Allows access to the Admit Card Students screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_marks_entry_view	exams_results.marks_entry.view	EXAMS_RESULTS	marks_entry	view	Access Marks Entry	Allows access to the Marks Entry screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_co_curricular_grades_view	exams_results.co_curricular_grades.view	EXAMS_RESULTS	co_curricular_grades	view	Access Co-Curricular Grades	Allows access to the Co-Curricular Grades screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_teacher_remark_view	exams_results.teacher_remark.view	EXAMS_RESULTS	teacher_remark	view	Access Teacher Remark	Allows access to the Teacher Remark screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_grade_list_view	exams_results.grade_list.view	EXAMS_RESULTS	grade_list	view	Access Grade List	Allows access to the Grade List screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_division_view	exams_results.division.view	EXAMS_RESULTS	division	view	Access Division	Allows access to the Division screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_exam_attendance_view	exams_results.exam_attendance.view	EXAMS_RESULTS	exam_attendance	view	Access Exam Attendance	Allows access to the Exam Attendance screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_report_card_view	exams_results.report_card.view	EXAMS_RESULTS	report_card	view	Access Report Card	Allows access to the Report Card screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_examination_reports_view	exams_results.examination_reports.view	EXAMS_RESULTS	examination_reports	view	Access Examination Reports	Allows access to the Examination Reports screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_exams_results_report_generation_view	exams_results.report_generation.view	EXAMS_RESULTS	report_generation	view	Access Report Generation	Allows access to the Report Generation screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_homework_homework_view	homework.homework.view	HOMEWORK	homework	view	Access HomeWork	Allows access to the HomeWork screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_homework_classwork_view	homework.classwork.view	HOMEWORK	classwork	view	Access ClassWork	Allows access to the ClassWork screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_homework_unassigned_report_view	homework.unassigned_report.view	HOMEWORK	unassigned_report	view	Access UnAssigned Report	Allows access to the UnAssigned Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_homework_hw_report_view	homework.hw_report.view	HOMEWORK	hw_report	view	Access HW Report	Allows access to the HW Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_homework_cw_report_view	homework.cw_report.view	HOMEWORK	cw_report	view	Access CW Report	Allows access to the CW Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_promote_student_view	academics.promote_student.view	ACADEMICS	promote_student	view	Access Promote Student	Allows access to the Promote Student screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_assign_class_teacher_view	academics.assign_class_teacher.view	ACADEMICS	assign_class_teacher	view	Access Assign Class Teacher	Allows access to the Assign Class Teacher screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_assign_subject_class_view	academics.assign_subject_class.view	ACADEMICS	assign_subject_class	view	Access Assign Subject Class	Allows access to the Assign Subject Class screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_student_assigned_sub_view	academics.student_assigned_sub.view	ACADEMICS	student_assigned_sub	view	Access Student Assigned Sub	Allows access to the Student Assigned Sub screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_teacher_timetable_view	academics.teacher_timetable.view	ACADEMICS	teacher_timetable	view	Access Teacher Timetable	Allows access to the Teacher Timetable screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_subjects_management_view	academics.subjects_management.view	ACADEMICS	subjects_management	view	Access Subjects Management	Allows access to the Subjects Management screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_class_view	academics.class.view	ACADEMICS	class	view	Access Class	Allows access to the Class screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_cocurricular_subjects_view	academics.cocurricular_subjects.view	ACADEMICS	cocurricular_subjects	view	Access CoCurricular Subjects	Allows access to the CoCurricular Subjects screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_class_timetable_view	academics.class_timetable.view	ACADEMICS	class_timetable	view	Access Class Timetable	Allows access to the Class Timetable screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_daily_class_timetable_view	academics.daily_class_timetable.view	ACADEMICS	daily_class_timetable	view	Access Daily Class Timetable	Allows access to the Daily Class Timetable screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_academics_section_management_view	academics.section_management.view	ACADEMICS	section_management	view	Access Section Management	Allows access to the Section Management screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_assignment_download_view	downloads.assignment_download.view	DOWNLOADS	assignment_download	view	Access Assignment Download	Allows access to the Assignment Download screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_study_material_view	downloads.study_material.view	DOWNLOADS	study_material	view	Access Study Material	Allows access to the Study Material screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_video_resources_view	downloads.video_resources.view	DOWNLOADS	video_resources	view	Access Video Resources	Allows access to the Video Resources screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_syllabus_download_view	downloads.syllabus_download.view	DOWNLOADS	syllabus_download	view	Access Syllabus Download	Allows access to the Syllabus Download screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_upload_content_view	downloads.upload_content.view	DOWNLOADS	upload_content	view	Access Upload Content	Allows access to the Upload Content screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_downloads_misc_downloads_view	downloads.misc_downloads.view	DOWNLOADS	misc_downloads	view	Access Misc Downloads	Allows access to the Misc Downloads screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_fees_collect_fees_view	fees.collect_fees.view	FEES	collect_fees	view	Access Collect Fees	Allows access to the Collect Fees screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_fees_fee_installments_view	fees.fee_installments.view	FEES	fee_installments	view	Access Fee Installments	Allows access to the Fee Installments screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_fees_search_due_fees_view	fees.search_due_fees.view	FEES	search_due_fees	view	Access Search Due Fees	Allows access to the Search Due Fees screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_fees_demand_notices_view	fees.demand_notices.view	FEES	demand_notices	view	Access Demand Notices	Allows access to the Demand Notices screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_fees_payment_receipts_view	fees.payment_receipts.view	FEES	payment_receipts	view	Access Payment Receipts	Allows access to the Payment Receipts screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_library_issue_return_report_view	library.issue_return_report.view	LIBRARY	issue_return_report	view	Access Issue Return Report	Allows access to the Issue Return Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_library_library_member_directory_view	library.library_member_directory.view	LIBRARY	library_member_directory	view	Access Library Member Directory	Allows access to the Library Member Directory screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_library_issue___return_dashboard_view	library.issue___return_dashboard.view	LIBRARY	issue___return_dashboard	view	Access Issue & Return Dashboard	Allows access to the Issue & Return Dashboard screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_library_book_list_view	library.book_list.view	LIBRARY	book_list	view	Access Book List	Allows access to the Book List screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_question_paper_question_type_view	question_paper.question_type.view	QUESTION_PAPER	question_type	view	Access Question Type	Allows access to the Question Type screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_question_paper_questions_repository_view	question_paper.questions_repository.view	QUESTION_PAPER	questions_repository	view	Access Questions Repository	Allows access to the Questions Repository screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_question_paper_generate_question_paper_view	question_paper.generate_question_paper.view	QUESTION_PAPER	generate_question_paper	view	Access Generate Question Paper	Allows access to the Generate Question Paper screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_communication_send_messages_view	communication.send_messages.view	COMMUNICATION	send_messages	view	Access Send Messages	Allows access to the Send Messages screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_communication_message_history_view	communication.message_history.view	COMMUNICATION	message_history	view	Access Message History	Allows access to the Message History screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_discipline_parameters_view	discipline.parameters.view	DISCIPLINE	parameters	view	Access Parameters	Allows access to the Parameters screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_discipline_assessment_view	discipline.assessment.view	DISCIPLINE	assessment	view	Access Assessment	Allows access to the Assessment screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_discipline_report_view	discipline.report.view	DISCIPLINE	report	view	Access Report	Allows access to the Report screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_inventory_add_new_stock_view	inventory.add_new_stock.view	INVENTORY	add_new_stock	view	Access Add New Stock	Allows access to the Add New Stock screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_inventory_items_management_view	inventory.items_management.view	INVENTORY	items_management	view	Access Items Management	Allows access to the Items Management screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_inventory_issue_item_view	inventory.issue_item.view	INVENTORY	issue_item	view	Access Issue Item	Allows access to the Issue Item screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_inventory_profit___loss_view	inventory.profit___loss.view	INVENTORY	profit___loss	view	Access Profit & Loss	Allows access to the Profit & Loss screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_settings_roles__legacy__view	settings.roles__legacy_.view	SETTINGS	roles__legacy_	view	Access Roles (Legacy)	Allows access to the Roles (Legacy) screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_settings_roles___permissions_view	settings.roles___permissions.view	SETTINGS	roles___permissions	view	Access Roles & Permissions	Allows access to the Roles & Permissions screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_settings_academic_setup_view	settings.academic_setup.view	SETTINGS	academic_setup	view	Access Academic Setup	Allows access to the Academic Setup screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_settings_support_view	settings.support.view	SETTINGS	support	view	Access Support	Allows access to the Support screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_staff_hr_departments_view	staff_hr.departments.view	STAFF_HR	departments	view	View Departments	View and manage HR departments	\N	f	f	t	t	2026-09-18 07:03:24.518628	["staff"]
perm_staff_hr_designations_view	staff_hr.designations.view	STAFF_HR	designations	view	View Designations	View and manage HR designations	\N	f	f	t	t	2026-09-18 07:03:24.518628	["staff"]
perm_staff_hr_recruitment_view	staff_hr.recruitment.view	STAFF_HR	recruitment	view	View Recruitment	View and manage Recruitment candidates	\N	f	f	t	t	2026-09-18 07:03:24.518628	["staff"]
perm_staff_hr_attendance_view	staff_hr.staff_attendance.view	STAFF_HR	staff_attendance	view	View Staff Attendance	View and manage Staff Attendance	\N	f	f	t	t	2026-09-18 07:03:24.518628	["staff"]
perm_staff_hr_tasks_view	staff_hr.staff_tasks.view	STAFF_HR	staff_tasks	view	View Staff Tasks	View and manage Staff Tasks	\N	f	f	t	t	2026-09-18 07:03:24.518628	["staff"]
perm_fees_my_class_fees_view	fees.my_class_fees.view	FEES	my_class_fees	view	Access My Class Fees	Allows access to the My Class Fees screen	["school"]	f	f	t	t	2026-09-18 07:03:24.594554	["staff"]
perm_fees_reminder_history_view	fees.reminder_history.view	FEES	reminder_history	view	Access Reminder History	Allows access to the Reminder History screen	["school"]	f	f	t	t	2026-09-18 07:03:24.594554	["staff"]
perm_fees_my_class_fees_send_reminder	fees.my_class_fees.send_reminder	FEES	my_class_fees	send_reminder	Send Fee Reminders	Allows sending fee reminders to parents	["school"]	f	f	t	t	2026-09-18 07:03:24.594554	["staff"]
perm_fees_my_class_fees_view_amount	fees.my_class_fees.view_amount	FEES	my_class_fees	view_amount	View Pending Amount	Allows viewing the actual pending fee amount of students	["school"]	f	t	t	t	2026-09-18 07:03:24.594554	["staff"]
perm_fees_student_fees_view	fees.student_fees.view	FEES	student_fees	view	Access Student Fees	Allows a student to view their own fees	["GLOBAL"]	f	f	t	t	2026-09-18 07:03:25.058406	["staff"]
perm_academic_timetable_period_view	academic.timetable_period.view	ACADEMICS	timetable_period	view	View Timetable Periods	View timetable periods configuration.	["school"]	f	f	t	t	2026-09-18 07:03:25.284076	["staff"]
perm_academic_timetable_period_edit	academic.timetable_period.edit	ACADEMICS	timetable_period	edit	Manage Timetable Periods	Create, edit, or delete timetable periods.	["school"]	f	t	t	t	2026-09-18 07:03:25.284076	["staff"]
perm_front_office_admission_enquiry_view	front_office.admission_enquiry.view	FRONT_OFFICE	admission_enquiry	view	Access Admission Enquiry	Allows access to the Admission Enquiry screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_visitor_book_view	front_office.visitor_book.view	FRONT_OFFICE	visitor_book	view	Access Visitor Book	Allows access to the Visitor Book screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_gate_pass_view	front_office.gate_pass.view	FRONT_OFFICE	gate_pass	view	Access Gate Pass	Allows access to the Gate Pass screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_entrance_exam_view	front_office.entrance_exam.view	FRONT_OFFICE	entrance_exam	view	Access Entrance Exam	Allows access to the Entrance Exam screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_parcel_receive_view	front_office.parcel_receive.view	FRONT_OFFICE	parcel_receive	view	Access Parcel Receive	Allows access to the Parcel Receive screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_parcel_dispatch_view	front_office.parcel_dispatch.view	FRONT_OFFICE	parcel_dispatch	view	Access Parcel Dispatch	Allows access to the Parcel Dispatch screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_complaints_view	front_office.complaints.view	FRONT_OFFICE	complaints	view	Access Complaints	Allows access to the Complaints screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_setup_front_office_view	front_office.setup_front_office.view	FRONT_OFFICE	setup_front_office	view	Access Setup Front Office	Allows access to the Setup Front Office screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_front_desk_view	front_office.front_desk.view	FRONT_OFFICE	front_desk	view	Access Front Desk	Allows access to the Front Desk screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_postal_records_view	front_office.postal_records.view	FRONT_OFFICE	postal_records	view	Access Postal Records	Allows access to the Postal Records screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_inventory_view	front_office.inventory.view	FRONT_OFFICE	inventory	view	Access Inventory	Allows access to the Inventory screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_front_office_library_view	front_office.library.view	FRONT_OFFICE	library	view	Access Library	Allows access to the Library screen	["school"]	f	f	f	t	2026-09-18 07:03:26.036174	["staff"]
perm_dashboard_link_siblings_view	students.link_siblings.view	STUDENTS	link_siblings	view	Access Link Siblings	Allows access to the Link Siblings screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_online_admission_view	students.online_admission.view	STUDENTS	online_admission	view	Access Online Admission	Allows access to the Online Admission screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_admission_view	students.student_admission.view	STUDENTS	student_admission	view	Access Student Admission	Allows access to the Student Admission screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_category_view	students.student_category.view	STUDENTS	student_category	view	Access Student Category	Allows access to the Student Category screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_directory_view	students.student_directory.view	STUDENTS	student_directory	view	Access All Students	Allows access to the All Students screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_house_view	students.student_house.view	STUDENTS	student_house	view	Access Student House	Allows access to the Student House screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_referral_view	students.student_referral.view	STUDENTS	student_referral	view	Access Student Referral	Allows access to the Student Referral screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_dashboard_student_reports_view	students.student_reports.view	STUDENTS	student_reports	view	Access Student Reports	Allows access to the Student Reports screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_attendance_subject_attendance_view	attendance.subject_attendance.view	ATTENDANCE	subject_attendance	view	View Subject Attendance	Allows viewing subject and period-wise attendance records	["school"]	f	f	t	t	2026-09-18 07:03:26.424489	["staff"]
perm_attendance_subject_attendance_edit	attendance.subject_attendance.edit	ATTENDANCE	subject_attendance	edit	Mark Subject Attendance	Allows teachers to record subject and period-wise attendance	["school"]	f	t	t	t	2026-09-18 07:03:26.424489	["staff"]
perm_exams_results_term_setup_edit	exams_results.term_setup.edit	EXAMS_RESULTS	term_setup	edit	Manage Term Setup	Allows configuring and modifying exam terms	["school"]	f	t	t	t	2026-09-18 07:03:26.519547	["staff"]
perm_exams_results_exam_setup_student_edit	exams_results.exam_setup_student.edit	EXAMS_RESULTS	exam_setup_student	edit	Manage Exam Setup	Allows configuring examination structures, grouping and weightage	["school"]	f	t	t	t	2026-09-18 07:03:26.519547	["staff"]
perm_exams_results_exam_schedule_student_edit	exams_results.exam_schedule_student.edit	EXAMS_RESULTS	exam_schedule_student	edit	Manage Exam Schedule	Allows creating and editing examination timetable schedules	["school"]	f	t	t	t	2026-09-18 07:03:26.519547	["staff"]
perm_exams_results_admit_card_students_edit	exams_results.admit_card_students.edit	EXAMS_RESULTS	admit_card_students	edit	Issue Admit Cards	Allows generating, printing and releasing admit cards	["school"]	f	t	t	t	2026-09-18 07:03:26.519547	["staff"]
perm_exams_results_marks_entry_edit	exams_results.marks_entry.edit	EXAMS_RESULTS	marks_entry	edit	Enter Exam Marks	Allows entering and updating student exam marks	["school"]	f	t	t	t	2026-09-18 07:03:26.652584	["staff"]
perm_exams_results_co_curricular_grades_edit	exams_results.co_curricular_grades.edit	EXAMS_RESULTS	co_curricular_grades	edit	Assess Co-Curricular Grades	Allows recording and updating co-curricular performance	["school"]	f	t	t	t	2026-09-18 07:03:26.652584	["staff"]
perm_exams_results_teacher_remark_edit	exams_results.teacher_remark.edit	EXAMS_RESULTS	teacher_remark	edit	Enter Teacher Remarks	Allows submitting qualitative feedback and remarks for students	["school"]	f	t	t	t	2026-09-18 07:03:26.652584	["staff"]
perm_exams_results_grade_list_edit	exams_results.grade_list.edit	EXAMS_RESULTS	grade_list	edit	Manage Grade Scales	Allows creating and configuring academic grade thresholds	["school"]	f	t	t	t	2026-09-18 07:03:26.652584	["staff"]
perm_exams_results_division_edit	exams_results.division.edit	EXAMS_RESULTS	division	edit	Manage Divisions	Allows creating and configuring academic divisions	["school"]	f	t	t	t	2026-09-18 07:03:26.652584	["staff"]
perm_exams_results_exam_attendance_edit	exams_results.exam_attendance.edit	EXAMS_RESULTS	exam_attendance	edit	Record Exam Attendance	Allows recording and updating examinee attendance and seating	["school"]	f	t	t	t	2026-09-18 07:03:26.806217	["staff"]
perm_exams_results_report_card_edit	exams_results.report_card.edit	EXAMS_RESULTS	report_card	edit	Generate Report Cards	Allows configuring and issuing student academic report cards	["school"]	f	t	t	t	2026-09-18 07:03:26.806217	["staff"]
perm_exams_results_examination_reports_edit	exams_results.examination_reports.edit	EXAMS_RESULTS	examination_reports	edit	Generate Examination Reports	Allows exporting and generating examination analytics and reports	["school"]	f	t	t	t	2026-09-18 07:03:26.806217	["staff"]
perm_exams_results_report_generation_edit	exams_results.report_generation.edit	EXAMS_RESULTS	report_generation	edit	Manage Report Generation	Allows managing batch processing and templates for report generation	["school"]	f	t	t	t	2026-09-18 07:03:26.806217	["staff"]
perm_income_view	income.transaction.view	ACCOUNTING	transaction	view	View Income	Allows viewing school income records	["school"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_income_edit	income.transaction.edit	ACCOUNTING	transaction	edit	Manage Income	Allows adding and managing income records	["school"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_expense_view	expense.transaction.view	ACCOUNTING	transaction	view	View Expenses	Allows viewing school expenses	["school"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_expense_edit	expense.transaction.edit	ACCOUNTING	transaction	edit	Manage Expenses	Allows adding and managing expense records	["school"]	f	t	t	t	2026-09-18 07:03:22.821862	["staff"]
perm_income_add_income_view	income.add_income.view	ACCOUNTING	add_income	view	Access Add Income	Allows access to the Add Income screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_income_search_income_view	income.search_income.view	ACCOUNTING	search_income	view	Access Search Income	Allows access to the Search Income screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_income_income_head_view	income.income_head.view	ACCOUNTING	income_head	view	Access Income Head	Allows access to the Income Head screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_expense_add_expense_view	expense.add_expense.view	ACCOUNTING	add_expense	view	Access Add Expense	Allows access to the Add Expense screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_expense_search_expense_view	expense.search_expense.view	ACCOUNTING	search_expense	view	Access Search Expense	Allows access to the Search Expense screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
perm_expense_expense_head_view	expense.expense_head.view	ACCOUNTING	expense_head	view	Access Expense Head	Allows access to the Expense Head screen	["school"]	f	f	t	t	2026-09-18 07:03:23.052534	["staff"]
student_exams_timetable_view	student.exams.timetable.view	EXAMS_RESULTS	EXAM_TIMETABLE	VIEW	View Exam Timetable	Allows student to view their exam timetable	["school", "assigned"]	f	f	t	t	2026-09-30 07:51:45.433824	["student", "parent"]
student_exams_admit_card_view	student.exams.admit_card.view	EXAMS_RESULTS	EXAM_ADMIT_CARD	VIEW	View Exam Admit Card	Allows student to view their admit card	["school", "assigned"]	f	f	t	t	2026-09-30 07:51:45.433824	["student", "parent"]
student_exams_results_view	student.exams.results.view	EXAMS_RESULTS	EXAM_RESULTS	VIEW	View Exam Results	Allows student to view their exam results	["school", "assigned"]	f	f	t	t	2026-09-30 07:51:45.433824	["student", "parent"]
student_exams_syllabus_view	student.exams.syllabus.view	EXAMS_RESULTS	EXAM_SYLLABUS	VIEW	View Exam Syllabus	Allows student to view their exam syllabus	["school", "assigned"]	f	f	t	t	2026-09-30 07:51:45.433824	["student", "parent"]
perm_certificates_view	certificates.certificate.view	CERTIFICATES	certificate	view	View Certificates	Allows viewing certificates and ID cards	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_onlineclass_view	online_class.session.view	ONLINE_CLASS	session	view	View Online Classes	Allows viewing scheduled online classes	["school", "class", "section", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_fee_view	fees.fee_invoice.view	FEES	fee_collection	view	View Fee Collections	Allows viewing fee transactions	["school", "linked"]	f	f	t	t	2026-09-18 07:03:22.660991	["staff", "student", "parent"]
perm_communication_send	communication.message.view	COMMUNICATION	message	send	Send Communications	Allows sending SMS/Emails to parents	["school", "class"]	f	t	t	t	2026-09-18 07:03:22.660991	["staff", "student", "parent"]
perm_lesson_view	lesson_module.plan.view	LESSON_MODULE	plan	view	View Lesson Plans	Allows viewing lesson plans and syllabus	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_downloads_view	downloads.material.view	DOWNLOADS	material	view	View Downloads	Allows viewing study materials and assignments	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_library_view	library.book.view	LIBRARY	book	view	View Library	Allows viewing library catalog and issued books	["school", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_questionpaper_view	question_paper.paper.view	QUESTION_PAPER	paper	view	View Question Papers	Allows viewing generated question papers	["school", "class"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_discipline_view	discipline.record.view	DISCIPLINE	record	view	View Discipline Records	Allows viewing student disciplinary records	["school", "class", "linked"]	f	f	t	t	2026-09-18 07:03:22.821862	["staff", "student", "parent"]
perm_leave_student_view	leave_management.student_leaves.view	LEAVE_MANAGEMENT	student_leaves	view	View Student Leaves	Allows access to student leave requests	\N	f	f	f	t	2026-10-09 10:56:36.601594	["school", "staff", "student", "parent"]
perm_leave_staff_view	leave_management.staff_leaves.view	LEAVE_MANAGEMENT	staff_leaves	view	View Staff Leaves	Allows access to staff leave requests	\N	f	f	f	t	2026-10-09 10:56:36.601594	["school", "staff"]
perm_homework_submission_view	homework.submission.view	HOMEWORK	SUBMISSION	VIEW	View Homework Submissions	Allows viewing homework and classwork student submissions	["school", "class", "linked"]	f	f	t	t	2026-10-09 18:57:19.355923	["staff"]
perm_homework_submission_edit	homework.submission.edit	HOMEWORK	SUBMISSION	EDIT	Submit Homework Answers	Allows students to submit homework/classwork answers and files	["school", "class", "linked"]	f	f	t	t	2026-10-09 18:57:19.355923	["staff"]
perm_homework_submission_grade	homework.submission.grade	HOMEWORK	SUBMISSION	GRADE	Grade Submissions	Allows teachers and staff to evaluate and grade student submissions	["school", "class"]	f	f	t	t	2026-10-09 18:57:19.355923	["staff"]
\.


--
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: neondb_owner
--

COPY public.role_permissions (id, school_id, role_id, permission_id, scope_type, scope_config) FROM stdin;
129	\N	role_super_admin_global	perm_attendance_view	school	{}
130	\N	role_super_admin_global	perm_attendance_edit	school	{}
134	\N	role_super_admin_global	perm_fee_refund	school	{}
135	\N	role_super_admin_global	perm_exam_view	school	{}
136	\N	role_super_admin_global	perm_exam_edit	school	{}
137	\N	role_super_admin_global	perm_homework_view	school	{}
138	\N	role_super_admin_global	perm_homework_edit	school	{}
139	\N	role_super_admin_global	perm_settings_edit	school	{}
140	\N	role_super_admin_global	perm_certificates_view	school	{}
141	\N	role_super_admin_global	perm_certificates_edit	school	{}
142	\N	role_super_admin_global	perm_onlineclass_view	school	{}
143	\N	role_super_admin_global	perm_onlineclass_edit	school	{}
144	\N	role_super_admin_global	perm_onlineexams_view	school	{}
145	\N	role_super_admin_global	perm_onlineexams_edit	school	{}
146	\N	role_super_admin_global	perm_fee_view	school	{}
147	\N	role_super_admin_global	perm_fee_edit	school	{}
150	\N	role_super_admin_global	perm_hr_view	school	{}
151	\N	role_super_admin_global	perm_hr_edit	school	{}
152	\N	role_super_admin_global	perm_communication_send	school	{}
153	\N	role_super_admin_global	perm_lesson_view	school	{}
154	\N	role_super_admin_global	perm_downloads_view	school	{}
155	\N	role_super_admin_global	perm_downloads_edit	school	{}
156	\N	role_super_admin_global	perm_income_view	school	{}
157	\N	role_super_admin_global	perm_income_edit	school	{}
158	\N	role_super_admin_global	perm_library_view	school	{}
159	\N	role_super_admin_global	perm_library_edit	school	{}
160	\N	role_super_admin_global	perm_expense_view	school	{}
161	\N	role_super_admin_global	perm_expense_edit	school	{}
162	\N	role_super_admin_global	perm_questionpaper_view	school	{}
163	\N	role_super_admin_global	perm_questionpaper_edit	school	{}
164	\N	role_super_admin_global	perm_discipline_view	school	{}
165	\N	role_super_admin_global	perm_discipline_edit	school	{}
166	\N	role_super_admin_global	perm_inventory_view	school	{}
167	\N	role_super_admin_global	perm_inventory_edit	school	{}
168	\N	role_super_admin_global	perm_lesson_edit	school	{}
169	\N	role_super_admin_global	perm_academics_view	school	{}
170	\N	role_super_admin_global	perm_academics_edit	school	{}
171	\N	role_super_admin_global	perm_dashboard_student_directory_view	school	{}
172	\N	role_super_admin_global	perm_dashboard_student_admission_view	school	{}
173	\N	role_super_admin_global	perm_dashboard_online_admission_view	school	{}
174	\N	role_super_admin_global	perm_dashboard_student_house_view	school	{}
175	\N	role_super_admin_global	perm_dashboard_student_reports_view	school	{}
176	\N	role_super_admin_global	perm_dashboard_link_siblings_view	school	{}
177	\N	role_super_admin_global	perm_dashboard_student_referral_view	school	{}
178	\N	role_super_admin_global	perm_dashboard_student_category_view	school	{}
179	\N	role_super_admin_global	perm_staff_hr_staff_directory_view	school	{}
180	\N	role_super_admin_global	perm_staff_hr_teacher_workload_view	school	{}
181	\N	role_super_admin_global	perm_staff_hr_payroll_view	school	{}
182	\N	role_super_admin_global	perm_attendance_attendance_register_view	school	{}
183	\N	role_super_admin_global	perm_attendance_attendance_analytics_view	school	{}
184	\N	role_super_admin_global	perm_attendance_attendance_my_records_view	school	{}
185	\N	role_super_admin_global	perm_attendance_student_attendance_view	school	{}
187	\N	role_super_admin_global	perm_attendance_attendance_reports_view	school	{}
196	\N	role_super_admin_global	perm_certificates_student_certificates_view	school	{}
197	\N	role_super_admin_global	perm_certificates_staff_certificates_view	school	{}
198	\N	role_super_admin_global	perm_certificates_student_id_cards_view	school	{}
199	\N	role_super_admin_global	perm_certificates_staff_id_cards_view	school	{}
200	\N	role_super_admin_global	perm_certificates_id_configuration_view	school	{}
201	\N	role_super_admin_global	perm_online_class_online_class_management_view	school	{}
202	\N	role_super_admin_global	perm_online_class_teacher_timetable_view	school	{}
203	\N	role_super_admin_global	perm_online_class_attendance_detailed_view	school	{}
204	\N	role_super_admin_global	perm_online_exams_create_exam_view	school	{}
205	\N	role_super_admin_global	perm_online_exams_question_bank_view	school	{}
206	\N	role_super_admin_global	perm_online_exams_exam_report_view	school	{}
207	\N	role_super_admin_global	perm_online_exams_student_exam_report_view	school	{}
208	\N	role_super_admin_global	perm_lesson_module_lesson_view	school	{}
209	\N	role_super_admin_global	perm_lesson_module_manage_planner_view	school	{}
210	\N	role_super_admin_global	perm_lesson_module_topic_view	school	{}
211	\N	role_super_admin_global	perm_lesson_module_topic_report_view	school	{}
212	\N	role_super_admin_global	perm_lesson_module_lesson_report_view	school	{}
213	\N	role_super_admin_global	perm_exams_results_term_setup_view	school	{}
214	\N	role_super_admin_global	perm_exams_results_exam_setup_student_view	school	{}
215	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
216	\N	role_super_admin_global	perm_exams_results_admit_card_students_view	school	{}
217	\N	role_super_admin_global	perm_exams_results_marks_entry_view	school	{}
218	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
297	\N	role_school_admin_global	perm_attendance_edit	school	{}
219	\N	role_super_admin_global	perm_exams_results_teacher_remark_view	school	{}
220	\N	role_super_admin_global	perm_exams_results_grade_list_view	school	{}
221	\N	role_super_admin_global	perm_exams_results_division_view	school	{}
222	\N	role_super_admin_global	perm_exams_results_exam_attendance_view	school	{}
223	\N	role_super_admin_global	perm_exams_results_report_card_view	school	{}
224	\N	role_super_admin_global	perm_exams_results_examination_reports_view	school	{}
225	\N	role_super_admin_global	perm_exams_results_report_generation_view	school	{}
226	\N	role_super_admin_global	perm_homework_homework_view	school	{}
227	\N	role_super_admin_global	perm_homework_classwork_view	school	{}
228	\N	role_super_admin_global	perm_homework_unassigned_report_view	school	{}
229	\N	role_super_admin_global	perm_homework_hw_report_view	school	{}
230	\N	role_super_admin_global	perm_homework_cw_report_view	school	{}
231	\N	role_super_admin_global	perm_academics_promote_student_view	school	{}
232	\N	role_super_admin_global	perm_academics_assign_class_teacher_view	school	{}
233	\N	role_super_admin_global	perm_academics_assign_subject_class_view	school	{}
234	\N	role_super_admin_global	perm_academics_student_assigned_sub_view	school	{}
235	\N	role_super_admin_global	perm_academics_teacher_timetable_view	school	{}
236	\N	role_super_admin_global	perm_academics_subjects_management_view	school	{}
237	\N	role_super_admin_global	perm_academics_class_view	school	{}
238	\N	role_super_admin_global	perm_academics_cocurricular_subjects_view	school	{}
239	\N	role_super_admin_global	perm_academics_class_timetable_view	school	{}
240	\N	role_super_admin_global	perm_academics_daily_class_timetable_view	school	{}
241	\N	role_super_admin_global	perm_academics_section_management_view	school	{}
242	\N	role_super_admin_global	perm_downloads_assignment_download_view	school	{}
243	\N	role_super_admin_global	perm_downloads_study_material_view	school	{}
244	\N	role_super_admin_global	perm_downloads_video_resources_view	school	{}
245	\N	role_super_admin_global	perm_downloads_syllabus_download_view	school	{}
246	\N	role_super_admin_global	perm_downloads_upload_content_view	school	{}
247	\N	role_super_admin_global	perm_downloads_misc_downloads_view	school	{}
248	\N	role_super_admin_global	perm_fees_collect_fees_view	school	{}
249	\N	role_super_admin_global	perm_fees_fee_installments_view	school	{}
250	\N	role_super_admin_global	perm_fees_search_due_fees_view	school	{}
251	\N	role_super_admin_global	perm_fees_demand_notices_view	school	{}
252	\N	role_super_admin_global	perm_fees_payment_receipts_view	school	{}
253	\N	role_super_admin_global	perm_income_add_income_view	school	{}
254	\N	role_super_admin_global	perm_income_search_income_view	school	{}
255	\N	role_super_admin_global	perm_income_income_head_view	school	{}
256	\N	role_super_admin_global	perm_library_issue_return_report_view	school	{}
257	\N	role_super_admin_global	perm_library_library_member_directory_view	school	{}
258	\N	role_super_admin_global	perm_library_issue___return_dashboard_view	school	{}
259	\N	role_super_admin_global	perm_library_book_list_view	school	{}
264	\N	role_super_admin_global	perm_expense_add_expense_view	school	{}
265	\N	role_super_admin_global	perm_expense_search_expense_view	school	{}
266	\N	role_super_admin_global	perm_expense_expense_head_view	school	{}
267	\N	role_super_admin_global	perm_question_paper_question_type_view	school	{}
268	\N	role_super_admin_global	perm_question_paper_questions_repository_view	school	{}
269	\N	role_super_admin_global	perm_question_paper_generate_question_paper_view	school	{}
270	\N	role_super_admin_global	perm_communication_send_messages_view	school	{}
271	\N	role_super_admin_global	perm_communication_message_history_view	school	{}
272	\N	role_super_admin_global	perm_discipline_parameters_view	school	{}
273	\N	role_super_admin_global	perm_discipline_assessment_view	school	{}
274	\N	role_super_admin_global	perm_discipline_report_view	school	{}
275	\N	role_super_admin_global	perm_inventory_add_new_stock_view	school	{}
276	\N	role_super_admin_global	perm_inventory_items_management_view	school	{}
277	\N	role_super_admin_global	perm_inventory_issue_item_view	school	{}
278	\N	role_super_admin_global	perm_inventory_profit___loss_view	school	{}
279	\N	role_super_admin_global	perm_settings_roles__legacy__view	school	{}
280	\N	role_super_admin_global	perm_settings_roles___permissions_view	school	{}
281	\N	role_super_admin_global	perm_settings_academic_setup_view	school	{}
282	\N	role_super_admin_global	perm_settings_support_view	school	{}
283	\N	role_super_admin_global	perm_staff_hr_departments_view	school	{}
284	\N	role_super_admin_global	perm_staff_hr_designations_view	school	{}
285	\N	role_super_admin_global	perm_staff_hr_recruitment_view	school	{}
286	\N	role_super_admin_global	perm_staff_hr_attendance_view	school	{}
288	\N	role_super_admin_global	perm_staff_hr_tasks_view	school	{}
289	\N	role_super_admin_global	perm_fees_my_class_fees_view	school	{}
290	\N	role_super_admin_global	perm_fees_reminder_history_view	school	{}
291	\N	role_super_admin_global	perm_fees_my_class_fees_send_reminder	school	{}
292	\N	role_super_admin_global	perm_fees_my_class_fees_view_amount	school	{}
293	\N	role_super_admin_global	perm_fees_student_fees_view	school	{}
294	\N	role_super_admin_global	perm_academic_timetable_period_view	school	{}
295	\N	role_super_admin_global	perm_academic_timetable_period_edit	school	{}
296	\N	role_school_admin_global	perm_attendance_view	school	{}
301	\N	role_school_admin_global	perm_fee_refund	school	{}
302	\N	role_school_admin_global	perm_exam_view	school	{}
303	\N	role_school_admin_global	perm_exam_edit	school	{}
304	\N	role_school_admin_global	perm_homework_view	school	{}
305	\N	role_school_admin_global	perm_homework_edit	school	{}
306	\N	role_school_admin_global	perm_settings_edit	school	{}
307	\N	role_school_admin_global	perm_certificates_view	school	{}
308	\N	role_school_admin_global	perm_certificates_edit	school	{}
309	\N	role_school_admin_global	perm_onlineclass_view	school	{}
310	\N	role_school_admin_global	perm_onlineclass_edit	school	{}
311	\N	role_school_admin_global	perm_onlineexams_view	school	{}
312	\N	role_school_admin_global	perm_onlineexams_edit	school	{}
313	\N	role_school_admin_global	perm_fee_view	school	{}
314	\N	role_school_admin_global	perm_fee_edit	school	{}
317	\N	role_school_admin_global	perm_hr_view	school	{}
318	\N	role_school_admin_global	perm_hr_edit	school	{}
319	\N	role_school_admin_global	perm_communication_send	school	{}
320	\N	role_school_admin_global	perm_lesson_view	school	{}
321	\N	role_school_admin_global	perm_downloads_view	school	{}
322	\N	role_school_admin_global	perm_downloads_edit	school	{}
323	\N	role_school_admin_global	perm_income_view	school	{}
324	\N	role_school_admin_global	perm_income_edit	school	{}
325	\N	role_school_admin_global	perm_library_view	school	{}
326	\N	role_school_admin_global	perm_library_edit	school	{}
327	\N	role_school_admin_global	perm_expense_view	school	{}
328	\N	role_school_admin_global	perm_expense_edit	school	{}
329	\N	role_school_admin_global	perm_questionpaper_view	school	{}
330	\N	role_school_admin_global	perm_questionpaper_edit	school	{}
331	\N	role_school_admin_global	perm_discipline_view	school	{}
332	\N	role_school_admin_global	perm_discipline_edit	school	{}
333	\N	role_school_admin_global	perm_inventory_view	school	{}
334	\N	role_school_admin_global	perm_inventory_edit	school	{}
335	\N	role_school_admin_global	perm_lesson_edit	school	{}
336	\N	role_school_admin_global	perm_academics_view	school	{}
337	\N	role_school_admin_global	perm_academics_edit	school	{}
338	\N	role_school_admin_global	perm_dashboard_student_directory_view	school	{}
339	\N	role_school_admin_global	perm_dashboard_student_admission_view	school	{}
340	\N	role_school_admin_global	perm_dashboard_online_admission_view	school	{}
341	\N	role_school_admin_global	perm_dashboard_student_house_view	school	{}
342	\N	role_school_admin_global	perm_dashboard_student_reports_view	school	{}
343	\N	role_school_admin_global	perm_dashboard_link_siblings_view	school	{}
344	\N	role_school_admin_global	perm_dashboard_student_referral_view	school	{}
345	\N	role_school_admin_global	perm_dashboard_student_category_view	school	{}
346	\N	role_school_admin_global	perm_staff_hr_staff_directory_view	school	{}
347	\N	role_school_admin_global	perm_staff_hr_teacher_workload_view	school	{}
348	\N	role_school_admin_global	perm_staff_hr_payroll_view	school	{}
349	\N	role_school_admin_global	perm_attendance_attendance_register_view	school	{}
350	\N	role_school_admin_global	perm_attendance_attendance_analytics_view	school	{}
351	\N	role_school_admin_global	perm_attendance_attendance_my_records_view	school	{}
352	\N	role_school_admin_global	perm_attendance_student_attendance_view	school	{}
354	\N	role_school_admin_global	perm_attendance_attendance_reports_view	school	{}
363	\N	role_school_admin_global	perm_certificates_student_certificates_view	school	{}
364	\N	role_school_admin_global	perm_certificates_staff_certificates_view	school	{}
365	\N	role_school_admin_global	perm_certificates_student_id_cards_view	school	{}
366	\N	role_school_admin_global	perm_certificates_staff_id_cards_view	school	{}
367	\N	role_school_admin_global	perm_certificates_id_configuration_view	school	{}
368	\N	role_school_admin_global	perm_online_class_online_class_management_view	school	{}
369	\N	role_school_admin_global	perm_online_class_teacher_timetable_view	school	{}
370	\N	role_school_admin_global	perm_online_class_attendance_detailed_view	school	{}
371	\N	role_school_admin_global	perm_online_exams_create_exam_view	school	{}
372	\N	role_school_admin_global	perm_online_exams_question_bank_view	school	{}
373	\N	role_school_admin_global	perm_online_exams_exam_report_view	school	{}
374	\N	role_school_admin_global	perm_online_exams_student_exam_report_view	school	{}
375	\N	role_school_admin_global	perm_lesson_module_lesson_view	school	{}
376	\N	role_school_admin_global	perm_lesson_module_manage_planner_view	school	{}
377	\N	role_school_admin_global	perm_lesson_module_topic_view	school	{}
378	\N	role_school_admin_global	perm_lesson_module_topic_report_view	school	{}
379	\N	role_school_admin_global	perm_lesson_module_lesson_report_view	school	{}
380	\N	role_school_admin_global	perm_exams_results_term_setup_view	school	{}
381	\N	role_school_admin_global	perm_exams_results_exam_setup_student_view	school	{}
382	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
383	\N	role_school_admin_global	perm_exams_results_admit_card_students_view	school	{}
384	\N	role_school_admin_global	perm_exams_results_marks_entry_view	school	{}
385	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
386	\N	role_school_admin_global	perm_exams_results_teacher_remark_view	school	{}
464	\N	role_teacher_global	perm_attendance_edit	school	{}
387	\N	role_school_admin_global	perm_exams_results_grade_list_view	school	{}
388	\N	role_school_admin_global	perm_exams_results_division_view	school	{}
389	\N	role_school_admin_global	perm_exams_results_exam_attendance_view	school	{}
390	\N	role_school_admin_global	perm_exams_results_report_card_view	school	{}
391	\N	role_school_admin_global	perm_exams_results_examination_reports_view	school	{}
392	\N	role_school_admin_global	perm_exams_results_report_generation_view	school	{}
393	\N	role_school_admin_global	perm_homework_homework_view	school	{}
394	\N	role_school_admin_global	perm_homework_classwork_view	school	{}
395	\N	role_school_admin_global	perm_homework_unassigned_report_view	school	{}
396	\N	role_school_admin_global	perm_homework_hw_report_view	school	{}
397	\N	role_school_admin_global	perm_homework_cw_report_view	school	{}
398	\N	role_school_admin_global	perm_academics_promote_student_view	school	{}
399	\N	role_school_admin_global	perm_academics_assign_class_teacher_view	school	{}
400	\N	role_school_admin_global	perm_academics_assign_subject_class_view	school	{}
401	\N	role_school_admin_global	perm_academics_student_assigned_sub_view	school	{}
402	\N	role_school_admin_global	perm_academics_teacher_timetable_view	school	{}
403	\N	role_school_admin_global	perm_academics_subjects_management_view	school	{}
404	\N	role_school_admin_global	perm_academics_class_view	school	{}
405	\N	role_school_admin_global	perm_academics_cocurricular_subjects_view	school	{}
406	\N	role_school_admin_global	perm_academics_class_timetable_view	school	{}
407	\N	role_school_admin_global	perm_academics_daily_class_timetable_view	school	{}
408	\N	role_school_admin_global	perm_academics_section_management_view	school	{}
409	\N	role_school_admin_global	perm_downloads_assignment_download_view	school	{}
410	\N	role_school_admin_global	perm_downloads_study_material_view	school	{}
411	\N	role_school_admin_global	perm_downloads_video_resources_view	school	{}
412	\N	role_school_admin_global	perm_downloads_syllabus_download_view	school	{}
413	\N	role_school_admin_global	perm_downloads_upload_content_view	school	{}
414	\N	role_school_admin_global	perm_downloads_misc_downloads_view	school	{}
415	\N	role_school_admin_global	perm_fees_collect_fees_view	school	{}
416	\N	role_school_admin_global	perm_fees_fee_installments_view	school	{}
417	\N	role_school_admin_global	perm_fees_search_due_fees_view	school	{}
418	\N	role_school_admin_global	perm_fees_demand_notices_view	school	{}
419	\N	role_school_admin_global	perm_fees_payment_receipts_view	school	{}
420	\N	role_school_admin_global	perm_income_add_income_view	school	{}
421	\N	role_school_admin_global	perm_income_search_income_view	school	{}
422	\N	role_school_admin_global	perm_income_income_head_view	school	{}
423	\N	role_school_admin_global	perm_library_issue_return_report_view	school	{}
424	\N	role_school_admin_global	perm_library_library_member_directory_view	school	{}
425	\N	role_school_admin_global	perm_library_issue___return_dashboard_view	school	{}
426	\N	role_school_admin_global	perm_library_book_list_view	school	{}
431	\N	role_school_admin_global	perm_expense_add_expense_view	school	{}
432	\N	role_school_admin_global	perm_expense_search_expense_view	school	{}
433	\N	role_school_admin_global	perm_expense_expense_head_view	school	{}
434	\N	role_school_admin_global	perm_question_paper_question_type_view	school	{}
435	\N	role_school_admin_global	perm_question_paper_questions_repository_view	school	{}
436	\N	role_school_admin_global	perm_question_paper_generate_question_paper_view	school	{}
437	\N	role_school_admin_global	perm_communication_send_messages_view	school	{}
438	\N	role_school_admin_global	perm_communication_message_history_view	school	{}
439	\N	role_school_admin_global	perm_discipline_parameters_view	school	{}
440	\N	role_school_admin_global	perm_discipline_assessment_view	school	{}
441	\N	role_school_admin_global	perm_discipline_report_view	school	{}
442	\N	role_school_admin_global	perm_inventory_add_new_stock_view	school	{}
443	\N	role_school_admin_global	perm_inventory_items_management_view	school	{}
444	\N	role_school_admin_global	perm_inventory_issue_item_view	school	{}
445	\N	role_school_admin_global	perm_inventory_profit___loss_view	school	{}
446	\N	role_school_admin_global	perm_settings_roles__legacy__view	school	{}
447	\N	role_school_admin_global	perm_settings_roles___permissions_view	school	{}
448	\N	role_school_admin_global	perm_settings_academic_setup_view	school	{}
449	\N	role_school_admin_global	perm_settings_support_view	school	{}
450	\N	role_school_admin_global	perm_staff_hr_departments_view	school	{}
451	\N	role_school_admin_global	perm_staff_hr_designations_view	school	{}
452	\N	role_school_admin_global	perm_staff_hr_recruitment_view	school	{}
453	\N	role_school_admin_global	perm_staff_hr_attendance_view	school	{}
455	\N	role_school_admin_global	perm_staff_hr_tasks_view	school	{}
456	\N	role_school_admin_global	perm_fees_my_class_fees_view	school	{}
457	\N	role_school_admin_global	perm_fees_reminder_history_view	school	{}
458	\N	role_school_admin_global	perm_fees_my_class_fees_send_reminder	school	{}
459	\N	role_school_admin_global	perm_fees_my_class_fees_view_amount	school	{}
460	\N	role_school_admin_global	perm_fees_student_fees_view	school	{}
461	\N	role_school_admin_global	perm_academic_timetable_period_view	school	{}
462	\N	role_school_admin_global	perm_academic_timetable_period_edit	school	{}
463	\N	role_teacher_global	perm_attendance_view	school	{}
466	\N	role_teacher_global	perm_exam_view	school	{}
467	\N	role_teacher_global	perm_exam_edit	school	{}
468	\N	role_teacher_global	perm_homework_view	school	{}
469	\N	role_teacher_global	perm_onlineclass_view	school	{}
470	\N	role_teacher_global	perm_lesson_view	school	{}
471	\N	role_teacher_global	perm_downloads_view	school	{}
472	\N	role_teacher_global	perm_academics_view	school	{}
473	\N	role_teacher_global	perm_exams_results_marks_entry_view	school	{}
474	\N	role_teacher_global	perm_exams_results_report_card_view	school	{}
475	\N	role_teacher_global	perm_academics_teacher_timetable_view	school	{}
476	\N	role_teacher_global	perm_academics_class_timetable_view	school	{}
477	\N	role_teacher_global	perm_fees_search_due_fees_view	school	{}
478	\N	role_teacher_global	perm_academic_timetable_period_view	school	{}
498	\N	role_parent_global	perm_exam_view	school	{}
499	\N	role_parent_global	perm_homework_view	school	{}
500	\N	role_parent_global	perm_onlineexams_view	school	{}
502	\N	role_parent_global	perm_communication_send	school	{}
504	\N	role_parent_global	perm_dashboard_student_reports_view	school	{}
505	\N	role_parent_global	perm_attendance_attendance_my_records_view	school	{}
506	\N	role_parent_global	perm_exams_results_exam_schedule_student_view	school	{}
507	\N	role_parent_global	perm_exams_results_report_card_view	school	{}
508	\N	role_parent_global	perm_academics_class_timetable_view	school	{}
509	\N	role_parent_global	perm_fees_search_due_fees_view	school	{}
510	\N	role_parent_global	perm_fees_payment_receipts_view	school	{}
550	\N	role_school_admin_global	perm_front_office_admission_enquiry_view	school	\N
551	\N	role_school_admin_global	perm_front_office_visitor_book_view	school	\N
552	\N	role_school_admin_global	perm_front_office_gate_pass_view	school	\N
553	\N	role_school_admin_global	perm_front_office_entrance_exam_view	school	\N
554	\N	role_school_admin_global	perm_front_office_parcel_receive_view	school	\N
555	\N	role_school_admin_global	perm_front_office_parcel_dispatch_view	school	\N
556	\N	role_school_admin_global	perm_front_office_complaints_view	school	\N
557	\N	role_school_admin_global	perm_front_office_setup_front_office_view	school	\N
558	\N	role_school_admin_global	perm_front_office_front_desk_view	school	\N
559	\N	role_school_admin_global	perm_front_office_postal_records_view	school	\N
560	\N	role_school_admin_global	perm_front_office_inventory_view	school	\N
561	\N	role_school_admin_global	perm_front_office_library_view	school	\N
565	\N	role_teacher_global	perm_attendance_attendance_register_view	school	{}
566	\N	role_teacher_global	perm_attendance_attendance_analytics_view	school	{}
567	\N	role_teacher_global	perm_attendance_student_attendance_view	school	{}
568	\N	role_parent_global	perm_fees_search_due_fees_view	school	{}
569	\N	role_parent_global	perm_fees_payment_receipts_view	school	{}
570	\N	role_parent_global	perm_fees_student_fees_view	school	{}
572	\N	role_teacher_global	perm_attendance_subject_attendance_view	school	{}
573	\N	role_teacher_global	perm_attendance_subject_attendance_edit	school	{}
574	\N	role_school_admin_global	perm_attendance_subject_attendance_view	school	{}
575	\N	role_school_admin_global	perm_attendance_subject_attendance_edit	school	{}
577	\N	role_parent_global	perm_attendance_subject_attendance_view	school	{}
578	\N	role_super_admin_global	perm_exams_results_admit_card_students_edit	school	{}
579	\N	role_super_admin_global	perm_exams_results_admit_card_students_view	school	{}
580	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
581	\N	role_super_admin_global	perm_exams_results_division_view	school	{}
582	\N	role_super_admin_global	perm_exams_results_exam_attendance_view	school	{}
583	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
584	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
585	\N	role_super_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
586	\N	role_super_admin_global	perm_exams_results_exam_setup_student_view	school	{}
587	\N	role_super_admin_global	perm_exams_results_examination_reports_view	school	{}
588	\N	role_super_admin_global	perm_exams_results_grade_list_view	school	{}
589	\N	role_super_admin_global	perm_exams_results_marks_entry_view	school	{}
590	\N	role_super_admin_global	perm_exams_results_report_card_view	school	{}
591	\N	role_super_admin_global	perm_exams_results_report_generation_view	school	{}
592	\N	role_super_admin_global	perm_exams_results_teacher_remark_view	school	{}
593	\N	role_super_admin_global	perm_exams_results_term_setup_edit	school	{}
594	\N	role_super_admin_global	perm_exams_results_term_setup_view	school	{}
595	\N	role_school_admin_global	perm_exams_results_admit_card_students_edit	school	{}
596	\N	role_school_admin_global	perm_exams_results_admit_card_students_view	school	{}
597	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
598	\N	role_school_admin_global	perm_exams_results_division_view	school	{}
599	\N	role_school_admin_global	perm_exams_results_exam_attendance_view	school	{}
600	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
601	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
602	\N	role_school_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
603	\N	role_school_admin_global	perm_exams_results_exam_setup_student_view	school	{}
604	\N	role_school_admin_global	perm_exams_results_examination_reports_view	school	{}
605	\N	role_school_admin_global	perm_exams_results_grade_list_view	school	{}
606	\N	role_school_admin_global	perm_exams_results_marks_entry_view	school	{}
607	\N	role_school_admin_global	perm_exams_results_report_card_view	school	{}
608	\N	role_school_admin_global	perm_exams_results_report_generation_view	school	{}
609	\N	role_school_admin_global	perm_exams_results_teacher_remark_view	school	{}
610	\N	role_school_admin_global	perm_exams_results_term_setup_edit	school	{}
611	\N	role_school_admin_global	perm_exams_results_term_setup_view	school	{}
612	\N	role_teacher_global	perm_exams_results_term_setup_view	school	{}
613	\N	role_teacher_global	perm_exams_results_exam_setup_student_view	school	{}
614	\N	role_teacher_global	perm_exams_results_exam_schedule_student_view	school	{}
615	\N	role_teacher_global	perm_exams_results_admit_card_students_view	school	{}
616	\N	role_super_admin_global	perm_exams_results_admit_card_students_edit	school	{}
617	\N	role_super_admin_global	perm_exams_results_admit_card_students_view	school	{}
618	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_edit	school	{}
619	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
620	\N	role_super_admin_global	perm_exams_results_division_edit	school	{}
621	\N	role_super_admin_global	perm_exams_results_division_view	school	{}
622	\N	role_super_admin_global	perm_exams_results_exam_attendance_view	school	{}
623	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
624	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
625	\N	role_super_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
626	\N	role_super_admin_global	perm_exams_results_exam_setup_student_view	school	{}
627	\N	role_super_admin_global	perm_exams_results_examination_reports_view	school	{}
628	\N	role_super_admin_global	perm_exams_results_grade_list_edit	school	{}
629	\N	role_super_admin_global	perm_exams_results_grade_list_view	school	{}
630	\N	role_super_admin_global	perm_exams_results_marks_entry_edit	school	{}
631	\N	role_super_admin_global	perm_exams_results_marks_entry_view	school	{}
632	\N	role_super_admin_global	perm_exams_results_report_card_view	school	{}
633	\N	role_super_admin_global	perm_exams_results_report_generation_view	school	{}
634	\N	role_super_admin_global	perm_exams_results_teacher_remark_edit	school	{}
635	\N	role_super_admin_global	perm_exams_results_teacher_remark_view	school	{}
636	\N	role_super_admin_global	perm_exams_results_term_setup_edit	school	{}
637	\N	role_super_admin_global	perm_exams_results_term_setup_view	school	{}
638	\N	role_school_admin_global	perm_exams_results_admit_card_students_edit	school	{}
639	\N	role_school_admin_global	perm_exams_results_admit_card_students_view	school	{}
640	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_edit	school	{}
641	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
642	\N	role_school_admin_global	perm_exams_results_division_edit	school	{}
643	\N	role_school_admin_global	perm_exams_results_division_view	school	{}
644	\N	role_school_admin_global	perm_exams_results_exam_attendance_view	school	{}
645	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
646	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
647	\N	role_school_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
648	\N	role_school_admin_global	perm_exams_results_exam_setup_student_view	school	{}
649	\N	role_school_admin_global	perm_exams_results_examination_reports_view	school	{}
650	\N	role_school_admin_global	perm_exams_results_grade_list_edit	school	{}
651	\N	role_school_admin_global	perm_exams_results_grade_list_view	school	{}
652	\N	role_school_admin_global	perm_exams_results_marks_entry_edit	school	{}
653	\N	role_school_admin_global	perm_exams_results_marks_entry_view	school	{}
654	\N	role_school_admin_global	perm_exams_results_report_card_view	school	{}
655	\N	role_school_admin_global	perm_exams_results_report_generation_view	school	{}
656	\N	role_school_admin_global	perm_exams_results_teacher_remark_edit	school	{}
657	\N	role_school_admin_global	perm_exams_results_teacher_remark_view	school	{}
658	\N	role_school_admin_global	perm_exams_results_term_setup_edit	school	{}
659	\N	role_school_admin_global	perm_exams_results_term_setup_view	school	{}
660	\N	role_teacher_global	perm_exams_results_marks_entry_view	school	{}
661	\N	role_teacher_global	perm_exams_results_co_curricular_grades_view	school	{}
662	\N	role_teacher_global	perm_exams_results_teacher_remark_view	school	{}
663	\N	role_teacher_global	perm_exams_results_grade_list_view	school	{}
664	\N	role_teacher_global	perm_exams_results_division_view	school	{}
665	\N	role_teacher_global	perm_exams_results_marks_entry_edit	school	{}
666	\N	role_teacher_global	perm_exams_results_co_curricular_grades_edit	school	{}
667	\N	role_teacher_global	perm_exams_results_teacher_remark_edit	school	{}
668	\N	role_super_admin_global	perm_exams_results_admit_card_students_edit	school	{}
669	\N	role_super_admin_global	perm_exams_results_admit_card_students_view	school	{}
670	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_edit	school	{}
671	\N	role_super_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
672	\N	role_super_admin_global	perm_exams_results_division_edit	school	{}
673	\N	role_super_admin_global	perm_exams_results_division_view	school	{}
674	\N	role_super_admin_global	perm_exams_results_exam_attendance_edit	school	{}
675	\N	role_super_admin_global	perm_exams_results_exam_attendance_view	school	{}
676	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
677	\N	role_super_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
678	\N	role_super_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
679	\N	role_super_admin_global	perm_exams_results_exam_setup_student_view	school	{}
680	\N	role_super_admin_global	perm_exams_results_examination_reports_edit	school	{}
681	\N	role_super_admin_global	perm_exams_results_examination_reports_view	school	{}
682	\N	role_super_admin_global	perm_exams_results_grade_list_edit	school	{}
683	\N	role_super_admin_global	perm_exams_results_grade_list_view	school	{}
684	\N	role_super_admin_global	perm_exams_results_marks_entry_edit	school	{}
685	\N	role_super_admin_global	perm_exams_results_marks_entry_view	school	{}
686	\N	role_super_admin_global	perm_exams_results_report_card_edit	school	{}
687	\N	role_super_admin_global	perm_exams_results_report_card_view	school	{}
688	\N	role_super_admin_global	perm_exams_results_report_generation_edit	school	{}
689	\N	role_super_admin_global	perm_exams_results_report_generation_view	school	{}
690	\N	role_super_admin_global	perm_exams_results_teacher_remark_edit	school	{}
691	\N	role_super_admin_global	perm_exams_results_teacher_remark_view	school	{}
692	\N	role_super_admin_global	perm_exams_results_term_setup_edit	school	{}
693	\N	role_super_admin_global	perm_exams_results_term_setup_view	school	{}
694	\N	role_school_admin_global	perm_exams_results_admit_card_students_edit	school	{}
695	\N	role_school_admin_global	perm_exams_results_admit_card_students_view	school	{}
696	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_edit	school	{}
697	\N	role_school_admin_global	perm_exams_results_co_curricular_grades_view	school	{}
698	\N	role_school_admin_global	perm_exams_results_division_edit	school	{}
699	\N	role_school_admin_global	perm_exams_results_division_view	school	{}
700	\N	role_school_admin_global	perm_exams_results_exam_attendance_edit	school	{}
701	\N	role_school_admin_global	perm_exams_results_exam_attendance_view	school	{}
702	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_edit	school	{}
703	\N	role_school_admin_global	perm_exams_results_exam_schedule_student_view	school	{}
704	\N	role_school_admin_global	perm_exams_results_exam_setup_student_edit	school	{}
705	\N	role_school_admin_global	perm_exams_results_exam_setup_student_view	school	{}
706	\N	role_school_admin_global	perm_exams_results_examination_reports_edit	school	{}
707	\N	role_school_admin_global	perm_exams_results_examination_reports_view	school	{}
708	\N	role_school_admin_global	perm_exams_results_grade_list_edit	school	{}
709	\N	role_school_admin_global	perm_exams_results_grade_list_view	school	{}
710	\N	role_school_admin_global	perm_exams_results_marks_entry_edit	school	{}
711	\N	role_school_admin_global	perm_exams_results_marks_entry_view	school	{}
712	\N	role_school_admin_global	perm_exams_results_report_card_edit	school	{}
713	\N	role_school_admin_global	perm_exams_results_report_card_view	school	{}
714	\N	role_school_admin_global	perm_exams_results_report_generation_edit	school	{}
715	\N	role_school_admin_global	perm_exams_results_report_generation_view	school	{}
716	\N	role_school_admin_global	perm_exams_results_teacher_remark_edit	school	{}
717	\N	role_school_admin_global	perm_exams_results_teacher_remark_view	school	{}
718	\N	role_school_admin_global	perm_exams_results_term_setup_edit	school	{}
719	\N	role_school_admin_global	perm_exams_results_term_setup_view	school	{}
720	\N	role_teacher_global	perm_exams_results_exam_attendance_view	school	{}
721	\N	role_teacher_global	perm_exams_results_report_card_view	school	{}
722	\N	role_teacher_global	perm_exams_results_examination_reports_view	school	{}
723	\N	role_teacher_global	perm_exams_results_exam_attendance_edit	school	{}
762	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_view	school	\N
763	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_edit	school	\N
764	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_promote_student_view	school	\N
765	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_student_assigned_sub_view	school	\N
766	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_teacher_timetable_view	school	\N
767	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_academics_class_timetable_view	school	\N
768	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_front_office_entrance_exam_view	school	\N
769	1001	a530710f-2cc9-4cd5-9d8b-e46eec079e80	perm_front_office_setup_front_office_view	school	\N
186	\N	role_super_admin_global	perm_leave_student_view	school	{}
353	\N	role_school_admin_global	perm_leave_student_view	school	{}
287	\N	role_super_admin_global	perm_leave_staff_view	school	{}
454	\N	role_school_admin_global	perm_leave_staff_view	school	{}
770	\N	role_super_admin_global	perm_homework_submission_view	school	{}
771	\N	role_super_admin_global	perm_homework_submission_grade	school	{}
772	\N	role_school_admin_global	perm_homework_submission_view	school	{}
773	\N	role_school_admin_global	perm_homework_submission_grade	school	{}
774	\N	role_teacher_global	perm_homework_submission_view	school	{}
775	\N	role_teacher_global	perm_homework_submission_grade	school	{}
778	\N	role_parent_global	perm_homework_submission_view	school	{}
779	\N	role_parent_global	perm_homework_submission_edit	school	{}
835	\N	role_student_global	perm_attendance_attendance_my_records_view	school	\N
836	\N	role_student_global	perm_attendance_view	school	\N
837	\N	role_student_global	perm_attendance_student_attendance_view	school	\N
838	\N	role_student_global	perm_attendance_subject_attendance_view	school	\N
839	\N	role_student_global	perm_homework_view	school	\N
840	\N	role_student_global	perm_homework_submission_view	school	\N
841	\N	role_student_global	perm_homework_submission_edit	school	\N
842	\N	role_student_global	perm_exam_view	school	\N
843	\N	role_student_global	perm_exams_results_exam_schedule_student_view	school	\N
844	\N	role_student_global	perm_exams_results_report_card_view	school	\N
845	\N	role_student_global	student_exams_timetable_view	school	\N
846	\N	role_student_global	student_exams_admit_card_view	school	\N
847	\N	role_student_global	student_exams_results_view	school	\N
848	\N	role_student_global	student_exams_syllabus_view	school	\N
849	\N	role_student_global	perm_fees_student_fees_view	school	\N
850	\N	role_student_global	perm_dashboard_student_reports_view	school	\N
851	\N	role_student_global	perm_academics_class_timetable_view	school	\N
852	\N	role_student_global	perm_academics_daily_class_timetable_view	school	\N
\.


--
-- Name: role_permissions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: neondb_owner
--

SELECT pg_catalog.setval('public.role_permissions_id_seq', 852, true);


--
-- PostgreSQL database dump complete
--

\unrestrict PRFbZxF7X3WFjjUai9IFnrUuj2fnPQ48NoglMZrpYrccxjzv0Wod1oQEaDQ1hOb

