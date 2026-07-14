# School ERP Access Control And Module System

This document explains how to design role-based access, module enable/disable support, and super admin/admin responsibilities for a complete School ERP.

It is written for someone starting from zero. After reading it, you should know what to build, why it exists, which decisions to make, what database tables are needed, and how every request should be checked.

## The Core Idea

A School ERP has many users and many schools.

Users should not all see the same things. A student should not manage fees. A teacher should not change billing settings. A school admin should not control another school. A platform super admin should control the whole SaaS platform.

So the system needs three layers of control:

| Layer | Question it answers | Example |
| --- | --- | --- |
| Tenant access | Which school can this user access? | User belongs to school `1`. |
| Role access | What type of user is this? | User is `ADMIN`, `TEACHER`, `PARENT`. |
| Module access | Is this feature enabled for this school? | Fee module is enabled, transport module is disabled. |

Every protected API should pass these checks:

```text
1. Is the user logged in?
2. Is the token valid?
3. Is the user allowed in this school?
4. Is the required module enabled for this school?
5. Does the user role/permission allow this action?
6. Is the requested data inside the same school?
```

If any check fails, reject the request.

## Current Backend Starting Point

The current backend already has a basic foundation.

Existing roles:

```java
SUPER_ADMIN
ADMIN
TEACHER
STUDENT
PARENT
```

Existing important tables:

| Table | Purpose |
| --- | --- |
| `users` | Stores global user profile. |
| `schools` | Stores school/tenant records. |
| `user_school_roles` | Connects a user to a school with a role. |
| `auth_sessions` | Stores access and refresh tokens. |

Existing token contains:

```text
userId
schoolId
role
```

This is good for a first version. For a full ERP, extend it with modules and permissions.

## Important Terms

### User

A real person who can log in.

Examples:

```text
School owner
Principal
Teacher
Student
Parent
Accountant
Receptionist
Platform operator
```

### School

A tenant in the system.

One deployed ERP can serve many schools. Each school must see only its own data.

### Role

A named responsibility.

Examples:

```text
SUPER_ADMIN
ADMIN
TEACHER
STUDENT
PARENT
ACCOUNTANT
RECEPTIONIST
LIBRARIAN
TRANSPORT_MANAGER
```

### Permission

A specific action the system allows.

Examples:

```text
student.read
student.create
student.update
student.delete
fee.read
fee.collect
attendance.mark
settings.update
```

### Module

A feature group that can be enabled or disabled.

Examples:

```text
student_management
attendance
fees
exams
transport
library
hostel
payroll
inventory
communication
```

## Recommended Decision

Use this model:

```text
User -> School Membership -> Role -> Permissions
School -> Enabled Modules
```

Do not rely only on hardcoded roles forever.

Use roles for broad user types, and permissions for exact actions.

Reason:

- Roles are easy for humans to understand.
- Permissions are easy for code to enforce.
- Modules allow schools to pay for or use only selected features.
- Super admin can control modules globally or per school.
- School admin can manage school users only within allowed modules.

## Final Role Model

Start with these roles:

| Role | Scope | Purpose |
| --- | --- | --- |
| `SUPER_ADMIN` | Platform/global | Owns the SaaS platform. Can manage all schools, plans, modules, and global settings. |
| `ADMIN` | School | Manages one school. Can configure users, classes, students, fees, attendance, and reports for that school. |
| `TEACHER` | School/class | Handles assigned classes, attendance, homework, marks, and student views. |
| `STUDENT` | Own profile | Views own attendance, fees, exams, homework, and notices. |
| `PARENT` | Own children | Views linked child records, fees, attendance, notices, and results. |
| `ACCOUNTANT` | School | Manages fees, invoices, payments, expenses, and financial reports. |
| `RECEPTIONIST` | School | Handles admissions, enquiries, visitor logs, and basic student details. |

Minimum viable ERP can keep only:

```text
SUPER_ADMIN
ADMIN
TEACHER
STUDENT
PARENT
```

But for a complete ERP, add:

```text
ACCOUNTANT
RECEPTIONIST
LIBRARIAN
TRANSPORT_MANAGER
HOSTEL_WARDEN
EXAM_CONTROLLER
HR_MANAGER
```

## Super Admin Responsibilities

`SUPER_ADMIN` controls the whole platform, not a single school.

Super admin can:

- Create, update, suspend, and delete schools.
- Enable or disable modules for any school.
- Assign subscription plans to schools.
- View platform-level dashboard.
- Manage platform users and support staff.
- Impersonate school admin only through audited support flow.
- See system logs and audit logs.
- Configure global module definitions.
- Configure global plans and pricing.
- Rotate or invalidate sessions when needed.

Super admin should not casually edit day-to-day school data unless there is a support reason.

Reason:

Super admin is a platform operator. School admin owns school operations.

## School Admin Responsibilities

`ADMIN` controls one school only.

Admin can:

- Manage school profile.
- Add teachers, staff, students, and parents.
- Assign roles within that school.
- Configure classes, sections, academic years, and fee heads.
- Use enabled modules.
- View reports for that school.
- Disable users in that school.
- Manage school-level settings.

Admin cannot:

- Access another school.
- Enable paid modules unless plan allows it.
- Change platform subscription plan.
- Create global modules.
- Read platform-wide data.

## Teacher Responsibilities

Teacher can:

- View assigned classes.
- View assigned students.
- Mark attendance for assigned classes.
- Add homework.
- Add marks if exam module allows it.
- View limited student details.

Teacher should not:

- Create school users.
- Delete students.
- Change fee settings.
- View full payment reports unless explicitly allowed.

## Student Responsibilities

Student can:

- View own profile.
- View own attendance.
- View own invoices and payment status.
- View own exam results.
- View homework and notices.

Student cannot:

- View other students.
- Update school data.
- Mark attendance.
- Access admin dashboards.

## Parent Responsibilities

Parent can:

- View linked children only.
- View child attendance.
- View child fees.
- View child exam results.
- Receive notices.
- Pay fees if payment module exists.

Parent cannot:

- View unrelated students.
- Access teacher/admin data.
- Modify school records.

## Module System

A module is a large ERP feature that can be turned on or off.

Recommended modules:

| Module Code | Feature |
| --- | --- |
| `student_management` | Students, parents, admissions, profiles. |
| `staff_management` | Staff records, roles, employment details. |
| `attendance` | Student/staff attendance. |
| `fees` | Invoices, payments, due reports. |
| `exams` | Exams, marks, grades, report cards. |
| `timetable` | Class timetable and teacher schedules. |
| `homework` | Homework and assignments. |
| `communication` | Notices, SMS, email, push notifications. |
| `transport` | Routes, vehicles, drivers, stops. |
| `library` | Books, issue/return, fines. |
| `hostel` | Rooms, beds, hostel fees. |
| `payroll` | Salary, deductions, payslips. |
| `inventory` | Assets, purchases, stock. |
| `reports` | Analytics and exports. |

## Module Enable/Disable Rules

Module checks should happen before permission checks.

Example:

```text
If fees module is disabled:
  Nobody can use fee APIs, even ADMIN.
```

Reason:

Module availability is a product/subscription decision. Permission is a user-level decision.

Correct order:

```text
1. Is module enabled?
2. Does role have permission?
```

## Recommended Database Tables

Add these tables over time.

### modules

Stores all possible modules in the platform.

```sql
CREATE TABLE modules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Example rows:

```text
student_management
attendance
fees
exams
transport
library
```

### school_modules

Stores which modules are enabled for each school.

```sql
CREATE TABLE school_modules (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    module_id BIGINT NOT NULL REFERENCES modules(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    enabled_by BIGINT REFERENCES users(id),
    enabled_at TIMESTAMP,
    disabled_at TIMESTAMP,
    UNIQUE (school_id, module_id)
);
```

### permissions

Stores all possible permissions.

```sql
CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(150) NOT NULL UNIQUE,
    module_code VARCHAR(100) NOT NULL,
    description TEXT
);
```

Example rows:

```text
student.read
student.create
student.update
student.delete
fee.read
fee.create
fee.collect
attendance.read
attendance.mark
settings.update
```

### role_permissions

Maps roles to permissions.

```sql
CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role user_role NOT NULL,
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    UNIQUE (role, permission_id)
);
```

This gives default permissions per role.

### user_permissions

Optional override table for special cases.

```sql
CREATE TABLE user_permissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    school_id BIGINT NOT NULL REFERENCES schools(id),
    permission_id BIGINT NOT NULL REFERENCES permissions(id),
    allowed BOOLEAN NOT NULL,
    UNIQUE (user_id, school_id, permission_id)
);
```

Use this only when needed.

Do not start with user-level custom permissions for everything. It makes admin screens complex. Start with role permissions.

### audit_logs

Required for admin/super admin safety.

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT REFERENCES schools(id),
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(150) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(100),
    user_agent TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

Use audit logs for:

- Role changes.
- Module enable/disable.
- User disable/enable.
- Fee changes.
- Student deletion.
- School suspension.
- Super admin actions.

## Permission Naming Convention

Use this pattern:

```text
resource.action
```

Examples:

```text
student.read
student.create
student.update
student.delete
staff.read
staff.create
fee.read
fee.create
fee.collect
attendance.read
attendance.mark
module.read
module.update
school.settings.update
```

Keep permissions small and clear.

Avoid vague permissions like:

```text
manage_all
admin_access
full_control
```

Vague permissions become dangerous later.

## Recommended Role Permission Matrix

Use this as the first version.

| Permission | SUPER_ADMIN | ADMIN | TEACHER | STUDENT | PARENT | ACCOUNTANT |
| --- | --- | --- | --- | --- | --- | --- |
| `school.read` | Yes | Own school | No | No | No | Own school |
| `school.create` | Yes | No | No | No | No | No |
| `school.update` | Yes | Own school limited | No | No | No | No |
| `module.read` | Yes | Own school | No | No | No | No |
| `module.update` | Yes | No | No | No | No | No |
| `user.read` | Yes | Own school | Limited | Own profile | Own profile | Limited |
| `user.create` | Yes | Own school | No | No | No | No |
| `user.disable` | Yes | Own school | No | No | No | No |
| `student.read` | Yes | Own school | Assigned classes | Self | Own children | Own school |
| `student.create` | Yes | Own school | No | No | No | No |
| `student.update` | Yes | Own school | Limited | No | No | No |
| `student.delete` | Yes | Own school | No | No | No | No |
| `attendance.read` | Yes | Own school | Assigned classes | Self | Own children | Own school |
| `attendance.mark` | Yes | Own school | Assigned classes | No | No | No |
| `fee.read` | Yes | Own school | No | Self | Own children | Own school |
| `fee.create` | Yes | Own school | No | No | No | Own school |
| `fee.collect` | Yes | Own school | No | No | No | Own school |
| `report.read` | Yes | Own school | Limited | No | No | Finance reports |

## How To Check Access In Code

Every protected endpoint should use an access guard.

Concept:

```text
requireAccess(moduleCode, permissionCode)
```

Example:

```text
GET /api/students
module = student_management
permission = student.read
```

Flow:

```text
1. JWT filter parses token.
2. AuthContext stores userId, schoolId, role.
3. Service/controller calls access guard.
4. Guard checks school module.
5. Guard checks role permission.
6. Service queries only data for token schoolId.
```

Pseudo-code:

```java
accessControl.require("student_management", "student.read");
```

Then service runs:

```java
Long schoolId = authContextService.resolveSchoolId(requestedSchoolId);
```

This prevents cross-school data access.

## API Response Rules

Use these status codes:

| Status | Meaning |
| --- | --- |
| `401 Unauthorized` | Missing, invalid, or expired token. |
| `403 Forbidden` | Token is valid but role/permission/module does not allow the action. |
| `404 Not Found` | Resource does not exist inside the user's school. |
| `400 Bad Request` | Request body or parameters are invalid. |

Important:

If user from school `1` requests student from school `2`, return `404` or `403`.

For security, prefer:

```text
404 Not Found
```

Reason:

It avoids revealing that another school's data exists.

## Super Admin School Context

There are two ways to handle `SUPER_ADMIN`.

Recommended approach:

```text
SUPER_ADMIN can operate globally only through /super-admin APIs.
SUPER_ADMIN must choose a school context before using school-level APIs.
```

Reason:

This keeps school-level logic simple and prevents accidental global access.

Example:

```text
/super-admin/schools
/super-admin/modules
/super-admin/schools/{schoolId}/modules
```

School APIs stay tenant-scoped:

```text
/api/students
/api/fees
/api/attendance
```

## Module Access Examples

### Fees Module Disabled

School has:

```text
fees = disabled
```

Then:

```text
GET /api/fee-invoices -> 403
POST /api/payments -> 403
```

Even if user role is `ADMIN`.

### Attendance Module Enabled

School has:

```text
attendance = enabled
```

Teacher has:

```text
attendance.mark
```

Then:

```text
POST /api/attendance -> allowed
```

Student does not have:

```text
attendance.mark
```

Then:

```text
POST /api/attendance -> 403
```

## User Enable/Disable

There are two different disable concepts.

### Disable User Globally

Use when the user should not log in anywhere.

Example:

```text
users.status = DISABLED
```

### Disable User In One School

Use when the user should lose access to only one school.

Example:

```text
user_school_roles.status = DISABLED
```

Recommended rule:

```text
Login checks users.status.
School selection checks user_school_roles.status.
```

This allows one phone/user to belong to multiple schools.

## Module Enable/Disable UI

Super admin screen:

```text
School > Modules
```

Show:

| Module | Status | Action |
| --- | --- | --- |
| Student Management | Enabled | Disable |
| Attendance | Enabled | Disable |
| Fees | Enabled | Disable |
| Transport | Disabled | Enable |

Admin screen:

```text
Settings > Modules
```

Admin can view enabled modules but should not enable paid modules unless product rules allow it.

Recommended decision:

```text
Only SUPER_ADMIN can enable/disable modules initially.
```

Add admin self-service later after subscription/billing is stable.

## Subscription Plan Support

Eventually, modules should connect to plans.

Example:

| Plan | Enabled Modules |
| --- | --- |
| Basic | Student, staff, attendance |
| Standard | Basic + fees + exams |
| Premium | Standard + transport + library + communication |

Add tables later:

```text
plans
plan_modules
school_subscriptions
```

Do not build billing too early. First build school_modules. Then add plans on top.

## Recommended Implementation Phases

### Phase 1: Stabilize Current Auth

Build:

- Current JWT login.
- User-school-role membership.
- Tenant isolation by `schoolId`.
- Basic roles.
- Admin can manage school data.

This project is already close to Phase 1.

### Phase 2: Add Module Tables

Build:

- `modules`
- `school_modules`
- seed default modules
- super admin APIs to enable/disable school modules
- module guard in backend

Do this before adding many ERP features.

### Phase 3: Add Permission Tables

Build:

- `permissions`
- `role_permissions`
- access guard service
- permission seed migration
- annotations or service-level checks

Do this before creating many staff roles.

### Phase 4: Add Admin Screens

Build:

- user management
- role assignment
- module view
- school settings
- audit log viewer

### Phase 5: Add Advanced Roles

Add:

- `ACCOUNTANT`
- `RECEPTIONIST`
- `LIBRARIAN`
- `TRANSPORT_MANAGER`
- `EXAM_CONTROLLER`

Only add roles when the module exists.

### Phase 6: Add Plans And Billing

Build:

- subscription plans
- plan modules
- renewal/expiry
- payment status
- automatic module disable on expiry

## Backend Package Design

Recommended packages:

```text
com.school.erp.security
com.school.erp.access
com.school.erp.module
com.school.erp.audit
com.school.erp.subscription
```

Recommended classes:

| Class | Purpose |
| --- | --- |
| `AccessControlService` | Checks module and permission. |
| `ModuleService` | Enables/disables modules per school. |
| `PermissionService` | Loads role permissions. |
| `AuditLogService` | Writes audit logs. |
| `SchoolSubscriptionService` | Handles plans later. |

## Recommended Access Guard Methods

Start with:

```java
requireModule(String moduleCode)
requirePermission(String permissionCode)
requireAccess(String moduleCode, String permissionCode)
requireRole(UserRole... roles)
```

Most APIs should use:

```java
requireAccess(moduleCode, permissionCode)
```

Only use `requireRole` for broad admin-only flows.

## Annotation Option

Later, you can create annotations:

```java
@RequireAccess(module = "fees", permission = "fee.collect")
```

Then use Spring AOP/interceptors to enforce it.

Do not start with annotations if the team is still moving quickly. A normal service call is easier to debug.

Recommended first version:

```java
accessControlService.requireAccess("fees", "fee.collect");
```

## Endpoint Design

Keep normal school APIs under:

```text
/api
```

Examples:

```text
/api/students
/api/staff
/api/attendance
/api/fee-invoices
```

Keep platform APIs under:

```text
/super-admin
```

Examples:

```text
/super-admin/schools
/super-admin/modules
/super-admin/schools/{schoolId}/modules
/super-admin/audit-logs
```

Reason:

It is easier to reason about global actions versus school actions.

## Data Isolation Rules

Every school-owned table must have:

```text
school_id
```

Examples:

```text
student.school_id
staff.school_id
fee_invoice.school_id
payment.school_id
student_attendance.school_id
```

Every query must filter by:

```text
school_id = token.schoolId
```

Never trust `schoolId` from request body alone.

Correct:

```text
effectiveSchoolId = authContext.resolveSchoolId(request.schoolId)
```

Wrong:

```text
repository.findBySchoolId(request.schoolId)
```

Reason:

The client can fake request values. The token is the trusted source.

## Admin Assignment Rules

Who can assign roles?

| Actor | Can assign |
| --- | --- |
| `SUPER_ADMIN` | Any role in any school. |
| `ADMIN` | School-level roles in own school, except `SUPER_ADMIN`. |
| `TEACHER` | Cannot assign roles. |
| `ACCOUNTANT` | Cannot assign roles. |
| `PARENT` | Cannot assign roles. |
| `STUDENT` | Cannot assign roles. |

Admin should not be able to create another super admin.

Recommended rule:

```text
Only existing SUPER_ADMIN can create another SUPER_ADMIN.
```

## Soft Delete Versus Hard Delete

For ERP systems, prefer soft delete for important records.

Use status:

```text
ACTIVE
INACTIVE
DISABLED
DELETED
```

Why:

- Schools need history.
- Fee and attendance records should not disappear.
- Audit trails matter.
- Reports depend on old data.

Hard delete is acceptable for temporary setup data, but not for financial or student history.

## Audit Requirements

Always audit:

- Login failures.
- Password/OTP changes.
- Role assignment.
- Module enable/disable.
- School status changes.
- Fee invoice changes.
- Payment creation.
- Student deletion/deactivation.
- User disable/enable.
- Super admin impersonation.

Audit log should store:

```text
who did it
which school
what action
which entity
old value
new value
when
ip address
user agent
```

## Recommended First Migration

When you implement this, create:

```text
V4__modules_permissions_audit.sql
```

Include:

- `modules`
- `school_modules`
- `permissions`
- `role_permissions`
- `audit_logs`
- seed module rows
- seed permission rows
- seed default role permissions
- enable default modules for existing schools

Do not edit old migrations already applied to Neon.

## Seed Default Modules

Start with:

```text
student_management
staff_management
attendance
fees
dashboard
reports
```

Enable these for existing schools.

Add advanced modules later:

```text
exams
transport
library
hostel
payroll
inventory
communication
```

## Seed Default Permissions

Start with:

```text
student.read
student.create
student.update
student.delete
staff.read
staff.create
staff.update
staff.delete
attendance.read
attendance.mark
fee.read
fee.create
fee.update
fee.delete
fee.collect
dashboard.read
report.read
module.read
module.update
user.read
user.create
user.update
user.disable
school.read
school.create
school.update
school.disable
```

## Practical Default Permissions

Give `SUPER_ADMIN`:

```text
all permissions
```

Give `ADMIN`:

```text
student.*
staff.*
attendance.*
fee.*
dashboard.read
report.read
user.*
school.read
school.update
module.read
```

Give `TEACHER`:

```text
student.read
attendance.read
attendance.mark
dashboard.read
```

Give `STUDENT`:

```text
student.self.read
attendance.self.read
fee.self.read
```

Give `PARENT`:

```text
student.children.read
attendance.children.read
fee.children.read
```

Give `ACCOUNTANT`:

```text
student.read
fee.read
fee.create
fee.update
fee.collect
report.read
```

## Frontend Rules

Frontend should use modules and permissions to show/hide UI.

But frontend checks are only for user experience.

Backend must still enforce everything.

Frontend should receive:

```json
{
  "role": "ADMIN",
  "schoolId": 1,
  "modules": ["student_management", "attendance", "fees"],
  "permissions": ["student.read", "student.create", "fee.read"]
}
```

Then frontend can hide:

- disabled modules
- buttons user cannot use
- routes user cannot open

Backend remains the source of truth.

## Example User Journeys

### Super Admin Enables Fees For A School

```text
1. Super admin logs in.
2. Opens /super-admin/schools.
3. Selects school.
4. Opens Modules tab.
5. Enables fees.
6. System updates school_modules.
7. System writes audit log.
8. School admin can now use fees APIs.
```

### Admin Adds A Teacher

```text
1. Admin logs in.
2. Selects school.
3. Backend token has role ADMIN and schoolId.
4. Admin creates user.
5. Admin assigns TEACHER role in same school.
6. System writes audit log.
```

### Teacher Marks Attendance

```text
1. Teacher logs in.
2. Teacher selects school.
3. Teacher calls attendance API.
4. Backend checks attendance module.
5. Backend checks attendance.mark permission.
6. Backend checks teacher is assigned to class.
7. Attendance is saved.
```

### Parent Views Child Fee

```text
1. Parent logs in.
2. Parent selects school.
3. Parent calls child fee API.
4. Backend checks fees module.
5. Backend checks parent-child link.
6. Backend returns only linked child fee records.
```

## What Not To Do

Do not make every role hardcoded in every controller.

Bad:

```text
if role == ADMIN then allow
```

Better:

```text
requireAccess("fees", "fee.collect")
```

Do not trust frontend hiding.

Do not let request `schoolId` override token `schoolId`.

Do not give school admin platform-level permissions.

Do not edit old Flyway migrations after deployment.

Do not build billing before basic module enable/disable works.

## Final Recommended Architecture

Use this as the target architecture:

```text
JWT token
  -> userId
  -> schoolId
  -> role

AuthContext
  -> current logged-in user

AccessControlService
  -> validates module
  -> validates permission
  -> validates role

School Modules
  -> controls feature availability per school

Role Permissions
  -> controls what each role can do

Audit Logs
  -> records sensitive admin actions
```

## Implementation Checklist

Build in this order:

1. Keep current login and school selection flow.
2. Add `modules` and `school_modules`.
3. Seed default modules.
4. Add `AccessControlService.requireModule`.
5. Protect fee, attendance, student, staff APIs with module checks.
6. Add `permissions` and `role_permissions`.
7. Add `AccessControlService.requireAccess`.
8. Replace role-only checks with permission checks.
9. Add super admin module management APIs.
10. Add audit logs.
11. Add admin user/role management APIs.
12. Add frontend module/permission response after login.
13. Add advanced roles only when modules need them.
14. Add plans and billing later.

## Decision Summary

Use:

```text
Multi-tenant school-based access
Role-based access for broad user type
Permission-based access for exact actions
Module enable/disable per school
Super admin APIs separate from school APIs
Audit logs for all sensitive changes
Flyway migrations for schema updates
```

Do not use:

```text
Only hardcoded roles forever
Frontend-only permissions
Global admin access inside normal school APIs
Request-body schoolId as trusted source
Hard delete for important ERP records
```

This model is flexible enough for a small school ERP now and a full SaaS ERP later.
