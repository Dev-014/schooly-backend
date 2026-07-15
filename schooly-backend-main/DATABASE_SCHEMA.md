# Schooly WebApp – Complete Database Schema Documentation

## Document Information

| Field         | Details                       |
| ------------- | ----------------------------- |
| Project       | Schooly WebApp                |
| Document Type | Database Schema Documentation |
| Architecture  | Relational Database Design    |
| Database Type | SQL-Based Relational Database |

---

# Overview

This document defines the production-level database schema for the Schooly WebApp backend. The design reflects the database state after applying upgrades for leave management, proper cascades, grade scale integration, and staff/teacher separation.

The schema supports:
* Normalized relational architecture with strict referential integrity
* Scalable multi-school (multi-tenant) model
* Role-based access control (RBAC) with linked permissions
* Student lifecycle & attendance tracking
* Financial operations (invoices and payments)
* Student/staff leave application & approval workflows
* Teacher management with qualifications & assignment mappings
* Exam grades resolved via customizable grade scales

---

# Core Modules Covered

* Authentication & Authorization (Multi-tenant)
* School Management (Tenants)
* Student Management (enrolment, sections, academic years)
* Staff & Teacher Management (separation of teaching profiles)
* Parent-Student Associations
* Class & Section Management
* Attendance Management
* Fee & Invoice Management
* Payment Tracking
* Exam & Grading Management (Grade scales mapping)
* Timetable Schedule Management
* Notifications & Messages
* Leave Management (Staff & Student leaves)
* Transport & Hostel Management
* Audit Logs

---

# Database Architecture

```text
schools
 ├── roles
 │    ├── user_school_roles (linked to users, schools, roles)
 │    └── roles_permissions (linked to roles)
 ├── users
 │    ├── auth_sessions
 │    ├── student_parents (bridge to student)
 │    ├── notifications
 │    └── messages
 ├── class
 │    ├── student
 │    │    ├── student_attendance
 │    │    ├── student_leaves
 │    │    ├── exam_results (linked to exams, subjects, grade_scales)
 │    │    ├── fee_invoice
 │    │    │    └── payment
 │    │    └── student_parents
 │    ├── exams
 │    └── timetable
 ├── sections
 ├── academic_years
 ├── staff
 │    ├── teachers (linked to teacher_subjects, timetable)
 │    └── staff_leaves
 └── transport / hostel
```

---

# Migration Files

- `src/main/resources/db/migration/V1__init.sql`
- `src/main/resources/db/migration/V2__multi_tenant_auth.sql`
- `src/main/resources/db/migration/V3__sample_data.sql`
- `src/main/resources/db/migration/V4__schema_improvements.sql`

---

# 1. schools Table

Stores school/tenant records. Supports soft delete.

| Column        | Type         | Constraints                       |
| ------------- | ------------ | ---------------------------------- |
| id            | BIGINT       | PRIMARY KEY                        |
| name          | VARCHAR(255) | NOT NULL                           |
| code          | VARCHAR(255) | NOT NULL UNIQUE                    |
| contact_email | VARCHAR(255) | NULL                               |
| contact_phone | VARCHAR(255) | NULL                               |
| address       | VARCHAR(255) | NULL                               |
| status        | VARCHAR(255) | NULL                               |
| created_at    | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP          |
| deleted_at    | TIMESTAMP    | NULL (Soft Delete)                 |

---

# 2. users Table

Stores authentication credentials and basic profile details.

| Column        | Type         | Constraints                        |
| ------------- | ------------ | ---------------------------------- |
| id            | BIGINT       | PRIMARY KEY                        |
| phone         | VARCHAR(20)  | NOT NULL UNIQUE                    |
| name          | VARCHAR(255) | NULL                               |
| email         | VARCHAR(255) | NULL                               |
| password_hash | VARCHAR(255) | NULL                               |
| status        | VARCHAR(50)  | NOT NULL DEFAULT 'ACTIVE'          |
| created_at    | TIMESTAMP    | NOT NULL DEFAULT CURRENT_TIMESTAMP |
| deleted_at    | TIMESTAMP    | NULL (Soft Delete)                 |

---

# 3. roles Table

Defines roles supported in the system. Ensures linked permissions.

| Column      | Type        | Constraints                        |
| ----------- | ----------- | ---------------------------------- |
| role        | user_role   | PRIMARY KEY                        |
| description | TEXT        | NULL                               |
| created_at  | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP          |
| updated_at  | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP          |
| deleted_at  | TIMESTAMP   | NULL (Soft Delete)                 |

---

# 4. user_school_roles Table

Links users to schools with specific roles.

| Column    | Type        | Constraints                                        |
| --------- | ----------- | -------------------------------------------------- |
| id        | BIGINT      | PRIMARY KEY                                        |
| user_id   | BIGINT      | FOREIGN KEY → `users(id)` ON DELETE CASCADE        |
| school_id | BIGINT      | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| role      | user_role   | FOREIGN KEY → `roles(role)` ON DELETE RESTRICT     |
| status    | VARCHAR(50) | NOT NULL DEFAULT 'ACTIVE'                          |
| joined_at | TIMESTAMP   | NOT NULL DEFAULT CURRENT_TIMESTAMP                 |
| left_at   | TIMESTAMP   | NULL                                               |
| deleted_at| TIMESTAMP   | NULL (Soft Delete)                                 |

* **Composite Unique**: `(user_id, school_id, role)`

---

# 5. class Table

Stores classes configured within schools.

| Column    | Type         | Constraints                                        |
| --------- | ------------ | -------------------------------------------------- |
| id        | BIGINT       | PRIMARY KEY                                        |
| name      | VARCHAR(255) | NOT NULL                                           |
| school_id | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| deleted_at| TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 6. sections Table

Defines class sections.

| Column     | Type         | Constraints                                        |
| ---------- | ------------ | -------------------------------------------------- |
| id         | BIGINT       | PRIMARY KEY                                        |
| school_id  | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| name       | VARCHAR(100) | NOT NULL                                           |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 7. academic_years Table

Stores academic calendar segments.

| Column      | Type         | Constraints                                        |
| ----------- | ------------ | -------------------------------------------------- |
| id          | BIGINT       | PRIMARY KEY                                        |
| school_id   | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| name        | VARCHAR(100) | NOT NULL                                           |
| start_date  | DATE         | NOT NULL                                           |
| end_date    | DATE         | NOT NULL                                           |
| status      | VARCHAR(50)  | NOT NULL DEFAULT 'ACTIVE'                          |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at  | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 8. student Table

Stores enrolled students. Integrates sections and academic years.

| Column           | Type         | Constraints                                             |
| ---------------- | ------------ | ------------------------------------------------------- |
| id               | BIGINT       | PRIMARY KEY                                             |
| user_id          | BIGINT       | NULL, FOREIGN KEY → `users(id)` ON DELETE RESTRICT      |
| name             | VARCHAR(255) | NULL                                                    |
| admission_no     | VARCHAR(255) | NOT NULL                                                |
| roll_number      | VARCHAR(255) | NULL                                                    |
| class_id         | BIGINT       | FOREIGN KEY → `class(id)` ON DELETE RESTRICT            |
| section_id       | BIGINT       | FOREIGN KEY → `sections(id)` ON DELETE SET NULL         |
| academic_year_id | BIGINT       | FOREIGN KEY → `academic_years(id)` ON DELETE SET NULL   |
| school_id        | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT          |
| status           | VARCHAR(255) | NOT NULL                                                |
| admission_date   | DATE         | NULL                                                    |
| deleted_at       | TIMESTAMP    | NULL (Soft Delete)                                      |

---

# 9. student_parents Table

Associates students with parents.

| Column         | Type        | Constraints                                         |
| -------------- | ----------- | --------------------------------------------------- |
| student_id     | BIGINT      | FOREIGN KEY → `student(id)` ON DELETE CASCADE       |
| parent_user_id | BIGINT      | FOREIGN KEY → `users(id)` ON DELETE CASCADE         |
| relation       | VARCHAR(100)| NULL                                                |
| is_primary     | BOOLEAN     | NOT NULL DEFAULT FALSE                              |

* **Composite Primary Key**: `(student_id, parent_user_id)`

---

# 10. staff Table

Represents school employees.

| Column         | Type          | Constraints                                        |
| -------------- | ------------- | -------------------------------------------------- |
| id             | BIGINT        | PRIMARY KEY                                        |
| user_id        | BIGINT        | FOREIGN KEY → `users(id)` ON DELETE RESTRICT       |
| school_id      | BIGINT        | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| department_id  | BIGINT        | NULL                                               |
| designation_id | BIGINT        | NULL                                               |
| joining_date   | DATE          | NULL                                               |
| salary         | NUMERIC(19, 2)| NULL                                               |
| status         | VARCHAR(255)  | NULL                                               |
| deleted_at     | TIMESTAMP     | NULL (Soft Delete)                                 |

---

# 11. teachers Table

Separated teaching profiles derived from staff records. Ensures only qualified staff teach classes.

| Column         | Type         | Constraints                                        |
| -------------- | ------------ | -------------------------------------------------- |
| id             | BIGINT       | PRIMARY KEY                                        |
| staff_id       | BIGINT       | UNIQUE, FOREIGN KEY → `staff(id)` ON DELETE RESTRICT|
| specialization | VARCHAR(255) | NULL                                               |
| qualification  | VARCHAR(255) | NULL                                               |
| status         | VARCHAR(50)  | NOT NULL DEFAULT 'ACTIVE'                          |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at     | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 12. subjects Table

Curriculum subjects.

| Column      | Type         | Constraints                                        |
| ----------- | ------------ | -------------------------------------------------- |
| id          | BIGINT       | PRIMARY KEY                                        |
| school_id   | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| name        | VARCHAR(255) | NOT NULL                                           |
| code        | VARCHAR(100) | NULL                                               |
| description | TEXT         | NULL                                               |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at  | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 13. teacher_subjects Table

Maps teachers to class sections and subjects.

| Column           | Type         | Constraints                                             |
| ---------------- | ------------ | ------------------------------------------------------- |
| id               | BIGINT       | PRIMARY KEY                                             |
| teacher_id       | BIGINT       | FOREIGN KEY → `teachers(id)` ON DELETE RESTRICT         |
| class_id         | BIGINT       | FOREIGN KEY → `class(id)` ON DELETE RESTRICT            |
| section_id       | BIGINT       | FOREIGN KEY → `sections(id)` ON DELETE SET NULL         |
| subject_id       | BIGINT       | FOREIGN KEY → `subjects(id)` ON DELETE RESTRICT         |
| academic_year_id | BIGINT       | FOREIGN KEY → `academic_years(id)` ON DELETE SET NULL   |
| created_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                               |
| deleted_at       | TIMESTAMP    | NULL (Soft Delete)                                      |

---

# 14. exams Table

Configured examinations.

| Column       | Type         | Constraints                                        |
| ------------ | ------------ | -------------------------------------------------- |
| id           | BIGINT       | PRIMARY KEY                                        |
| school_id    | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| name         | VARCHAR(255) | NOT NULL                                           |
| exam_type    | VARCHAR(100) | NULL                                               |
| class_id     | BIGINT       | FOREIGN KEY → `class(id)` ON DELETE RESTRICT       |
| section_id   | BIGINT       | FOREIGN KEY → `sections(id)` ON DELETE SET NULL    |
| starts_at    | TIMESTAMP    | NOT NULL                                           |
| ends_at      | TIMESTAMP    | NOT NULL                                           |
| status       | VARCHAR(50)  | NOT NULL DEFAULT 'SCHEDULED'                       |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at   | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 15. grade_scales Table

Maps scoring boundaries to descriptive letter grades.

| Column      | Type          | Constraints                                        |
| ----------- | ------------- | -------------------------------------------------- |
| id          | BIGINT        | PRIMARY KEY                                        |
| school_id   | BIGINT        | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| grade       | VARCHAR(10)   | NOT NULL                                           |
| min_mark    | NUMERIC(5, 2) | NOT NULL                                           |
| max_mark    | NUMERIC(5, 2) | NOT NULL                                           |
| grade_point | NUMERIC(3, 2) | NULL                                               |
| description | VARCHAR(255)  | NULL                                               |
| created_at  | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at  | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at  | TIMESTAMP     | NULL (Soft Delete)                                 |

* **Constraints**: `min_mark <= max_mark`
* **Composite Unique**: `(school_id, grade)`

---

# 16. exam_results Table

Holds student exam scores resolved via grade scales.

| Column         | Type          | Constraints                                             |
| -------------- | ------------- | ------------------------------------------------------- |
| id             | BIGINT        | PRIMARY KEY                                             |
| exam_id        | BIGINT        | FOREIGN KEY → `exams(id)` ON DELETE RESTRICT            |
| student_id     | BIGINT        | FOREIGN KEY → `student(id)` ON DELETE RESTRICT          |
| subject_id     | BIGINT        | FOREIGN KEY → `subjects(id)` ON DELETE RESTRICT         |
| marks_obtained | NUMERIC(6, 2) | NULL                                                    |
| max_marks      | NUMERIC(6, 2) | NULL                                                    |
| grade_scale_id | BIGINT        | FOREIGN KEY → `grade_scales(id)` ON DELETE SET NULL     |
| remarks        | TEXT          | NULL                                                    |
| status         | VARCHAR(50)   | NOT NULL DEFAULT 'PENDING'                              |
| created_at     | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                               |
| deleted_at     | TIMESTAMP     | NULL (Soft Delete)                                      |

---

# 17. timetable Table

Schedules daily class periods.

| Column       | Type         | Constraints                                        |
| ------------ | ------------ | -------------------------------------------------- |
| id           | BIGINT       | PRIMARY KEY                                        |
| school_id    | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| class_id     | BIGINT       | FOREIGN KEY → `class(id)` ON DELETE RESTRICT       |
| section_id   | BIGINT       | FOREIGN KEY → `sections(id)` ON DELETE SET NULL    |
| subject_id   | BIGINT       | FOREIGN KEY → `subjects(id)` ON DELETE RESTRICT     |
| teacher_id   | BIGINT       | FOREIGN KEY → `teachers(id)` ON DELETE RESTRICT    |
| day_of_week  | VARCHAR(20)  | NOT NULL                                           |
| start_time   | TIME         | NOT NULL                                           |
| end_time     | TIME         | NOT NULL                                           |
| room         | VARCHAR(100) | NULL                                               |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at   | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 18. student_attendance Table

Tracks student presence records.

| Column          | Type         | Constraints                                        |
| --------------- | ------------ | -------------------------------------------------- |
| id              | BIGINT       | PRIMARY KEY                                        |
| student_id      | BIGINT       | FOREIGN KEY → `student(id)` ON DELETE CASCADE      |
| school_id       | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| attendance_date | DATE         | NOT NULL                                           |
| status          | VARCHAR(255) | NOT NULL                                           |
| deleted_at      | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 19. fee_invoice Table

Generated school fees invoice records.

| Column          | Type          | Constraints                                        |
| --------------- | ------------- | -------------------------------------------------- |
| id              | BIGINT        | PRIMARY KEY                                        |
| student_id      | BIGINT        | FOREIGN KEY → `student(id)` ON DELETE RESTRICT     |
| school_id       | BIGINT        | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| academic_year_id| BIGINT        | NULL                                               |
| due_date        | DATE          | NOT NULL                                           |
| total_amount    | NUMERIC(19, 2)| NOT NULL                                           |
| paid_amount     | NUMERIC(19, 2)| NULL                                               |
| status          | VARCHAR(255)  | NOT NULL                                           |
| created_at      | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at      | TIMESTAMP     | NULL (Soft Delete)                                 |

---

# 20. payment Table

Monitors payments registered against fee invoices.

| Column         | Type          | Constraints                                        |
| -------------- | ------------- | -------------------------------------------------- |
| id             | BIGINT        | PRIMARY KEY                                        |
| invoice_id     | BIGINT        | FOREIGN KEY → `fee_invoice(id)` ON DELETE RESTRICT |
| school_id      | BIGINT        | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| amount         | NUMERIC(19, 2)| NOT NULL                                           |
| payment_mode   | VARCHAR(255)  | NOT NULL                                           |
| transaction_id | VARCHAR(255)  | NULL                                               |
| status         | VARCHAR(255)  | NULL                                               |
| created_at     | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at     | TIMESTAMP     | NULL (Soft Delete)                                 |

---

# 21. staff_leaves Table

Manages staff leave applications and approval status.

| Column       | Type        | Constraints                                        |
| ------------ | ----------- | -------------------------------------------------- |
| id           | BIGINT      | PRIMARY KEY                                        |
| staff_id     | BIGINT      | FOREIGN KEY → `staff(id)` ON DELETE RESTRICT       |
| school_id    | BIGINT      | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| leave_type   | VARCHAR(100)| NOT NULL (e.g. CASUAL, SICK, SABBATICAL)           |
| start_date   | DATE        | NOT NULL                                           |
| end_date     | DATE        | NOT NULL                                           |
| reason       | TEXT        | NULL                                               |
| status       | VARCHAR(50) | NOT NULL DEFAULT 'PENDING'                         |
| approved_by  | BIGINT      | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| created_at   | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at   | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at   | TIMESTAMP   | NULL (Soft Delete)                                 |

---

# 22. student_leaves Table

Manages student leave requests.

| Column      | Type        | Constraints                                        |
| ----------- | ----------- | -------------------------------------------------- |
| id          | BIGINT      | PRIMARY KEY                                        |
| student_id  | BIGINT      | FOREIGN KEY → `student(id)` ON DELETE RESTRICT     |
| school_id   | BIGINT      | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| start_date  | DATE        | NOT NULL                                           |
| end_date    | DATE        | NOT NULL                                           |
| reason      | TEXT        | NULL                                               |
| status      | VARCHAR(50) | NOT NULL DEFAULT 'PENDING'                         |
| approved_by | BIGINT      | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| created_at  | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at  | TIMESTAMP   | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at  | TIMESTAMP   | NULL (Soft Delete)                                 |

---

# 23. auth_sessions Table

Tracks authentication sessions and device context.

| Column         | Type       | Constraints                                        |
| -------------- | ---------- | -------------------------------------------------- |
| id             | BIGINT     | PRIMARY KEY                                        |
| user_id        | BIGINT     | FOREIGN KEY → `users(id)` ON DELETE CASCADE        |
| school_id      | BIGINT     | FOREIGN KEY → `schools(id)` ON DELETE CASCADE      |
| access_token   | TEXT       | NOT NULL                                           |
| refresh_token  | TEXT       | NOT NULL                                           |
| device_info    | JSONB      | NULL                                               |
| created_at     | TIMESTAMP  | NOT NULL DEFAULT CURRENT_TIMESTAMP                 |

---

# 24. notifications Table

Houses system announcements and personal user notifications.

| Column         | Type         | Constraints                                        |
| -------------- | ------------ | -------------------------------------------------- |
| id             | BIGINT       | PRIMARY KEY                                        |
| school_id      | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| sender_id      | BIGINT       | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| recipient_id   | BIGINT       | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| recipient_type | VARCHAR(50)  | NOT NULL                                           |
| title          | VARCHAR(255) | NOT NULL                                           |
| message        | TEXT         | NOT NULL                                           |
| status         | VARCHAR(50)  | NOT NULL DEFAULT 'UNREAD'                          |
| sent_at        | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| read_at        | TIMESTAMP    | NULL                                               |
| deleted_at     | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 25. roles_permissions Table

Stores advanced role-based permissions, linked to the `roles` table.

| Column       | Type         | Constraints                                        |
| ------------ | ------------ | -------------------------------------------------- |
| id           | BIGINT       | PRIMARY KEY                                        |
| role         | user_role    | FOREIGN KEY → `roles(role)` ON DELETE CASCADE      |
| permission   | VARCHAR(255) | NOT NULL                                           |
| description  | TEXT         | NULL                                               |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at   | TIMESTAMP    | NULL (Soft Delete)                                 |

* **Composite Unique**: `(role, permission)`

---

# 26. transport Table

Registers vehicle details and drivers.

| Column         | Type         | Constraints                                        |
| -------------- | ------------ | -------------------------------------------------- |
| id             | BIGINT       | PRIMARY KEY                                        |
| school_id      | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| vehicle_number | VARCHAR(100) | NOT NULL                                           |
| driver_name    | VARCHAR(255) | NULL                                               |
| driver_phone   | VARCHAR(20)  | NULL                                               |
| route_name     | VARCHAR(255) | NULL                                               |
| capacity       | INT          | NULL                                               |
| status         | VARCHAR(50)  | NOT NULL DEFAULT 'ACTIVE'                          |
| created_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at     | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 27. hostel Table

Monitors lodging space.

| Column       | Type         | Constraints                                        |
| ------------ | ------------ | -------------------------------------------------- |
| id           | BIGINT       | PRIMARY KEY                                        |
| school_id    | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| name         | VARCHAR(255) | NOT NULL                                           |
| block        | VARCHAR(100) | NULL                                               |
| total_rooms  | INT          | NULL                                               |
| capacity     | INT          | NULL                                               |
| warden_name  | VARCHAR(255) | NULL                                               |
| status       | VARCHAR(50)  | NOT NULL DEFAULT 'ACTIVE'                          |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| updated_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| deleted_at   | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 28. messages Table

Monitors message conversations between users.

| Column        | Type         | Constraints                                        |
| ------------- | ------------ | -------------------------------------------------- |
| id            | BIGINT       | PRIMARY KEY                                        |
| school_id     | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE RESTRICT     |
| sender_id     | BIGINT       | FOREIGN KEY → `users(id)` ON DELETE RESTRICT       |
| recipient_id  | BIGINT       | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| subject       | VARCHAR(255) | NULL                                               |
| body          | TEXT         | NOT NULL                                           |
| status        | VARCHAR(50)  | NOT NULL DEFAULT 'SENT'                            |
| sent_at       | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |
| read_at       | TIMESTAMP    | NULL                                               |
| deleted_at    | TIMESTAMP    | NULL (Soft Delete)                                 |

---

# 29. audit_logs Table

Tracks backend activity records.

| Column       | Type         | Constraints                                        |
| ------------ | ------------ | -------------------------------------------------- |
| id           | BIGINT       | PRIMARY KEY                                        |
| school_id    | BIGINT       | FOREIGN KEY → `schools(id)` ON DELETE SET NULL     |
| user_id      | BIGINT       | FOREIGN KEY → `users(id)` ON DELETE SET NULL       |
| action       | VARCHAR(255) | NOT NULL                                           |
| entity_name  | VARCHAR(100) | NULL                                               |
| entity_id    | VARCHAR(100) | NULL                                               |
| details      | JSONB        | NULL                                               |
| ip_address   | VARCHAR(100) | NULL                                               |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                          |

---

# Entity Relationship Summary

| Table              | Related Tables (via Foreign Keys)                                |
| ------------------ | ---------------------------------------------------------------- |
| schools            | users, class, student, staff, fee_invoice, sections, academic_years, subjects, exams, timetable, notifications, transport, hostel, messages, audit_logs, staff_leaves, student_leaves, grade_scales |
| users              | user_school_roles, auth_sessions, student_parents, staff, messages, audit_logs, notifications, staff_leaves, student_leaves |
| roles              | user_school_roles, roles_permissions                              |
| class              | student, timetable, teacher_subjects, exams                       |
| sections           | student, teacher_subjects, exams, timetable                       |
| academic_years     | student, teacher_subjects, fee_invoice                            |
| student            | student_attendance, fee_invoice, student_parents, exam_results, student_leaves |
| student_parents    | users, student                                                   |
| staff              | teachers, staff_leaves                                           |
| teachers           | staff, teacher_subjects, timetable                               |
| subjects           | teacher_subjects, timetable, exam_results                        |
| fee_invoice        | payment                                                          |
| exams              | exam_results                                                     |
| grade_scales       | exam_results                                                     |

---

# Recommended Indexes

| Table             | Indexed Column  |
| ----------------- | --------------- |
| users             | phone           |
| class             | school_id       |
| student           | class_id        |
| student           | school_id       |
| fee_invoice       | student_id      |
| payment           | invoice_id      |
| student_attendance| attendance_date |
| subjects          | code            |
| exams             | school_id       |
| notifications     | recipient_id    |
| audit_logs        | user_id         |
| teachers          | staff_id        |
| roles_permissions | role            |

---

# Security Recommendations

## Database Security

* Store passwords securely using BCrypt.
* Apply role-based access control (RBAC) filtering using `user_school_roles` and permissions defined in `roles_permissions`.
* Restrict database query access to backend processes.
* Perform weekly schema backups with point-in-time recovery.
