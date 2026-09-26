# Schooly HR Module — Frontend API Integration Guide

This guide provides end-to-end documentation for integrating the **Human Resource (HR) & Staff Management** module into `schooly-web`. It covers all **38 REST APIs** spanning the 10 HR functional domains.

---

## 1. General Architecture & Standards

### 1.1 Base URL & Environment
- **Development Server**: `http://localhost:8080` (or Vite proxy `/api`)
- **API Prefix**: `/api/v1`

### 1.2 Authentication & Headers
All requests must include the JWT authentication token in the standard Bearer format:
```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
Accept: application/json
```

### 1.3 Multi-Tenant School ID Routing (Dual-Path Support)
To maximize flexibility across route parameters and global app state, all endpoints support **both** URL routing conventions:
1. **Path-based tenant routing (Recommended for multi-tenant views)**:
   ```http
   GET /api/v1/admin/schools/{schoolId}/hr/staff
   ```
2. **Query-parameter fallback**:
   ```http
   GET /api/v1/admin/hr/staff?schoolId={schoolId}
   ```
> If `schoolId` is present in the path, it takes precedence. Otherwise, the backend automatically reads `?schoolId=...` from the query string.

---

## 2. API Reference by Functional Category

---

### Category 1: Staff Directory & Profile Management

#### 1.1 List Staff Members
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff` (or `/api/v1/admin/hr/staff?schoolId={schoolId}`)
- **Permission**: `staff_hr.staff_directory.view`
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "schoolId": 1,
    "staffCode": "STF-001",
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@school.edu",
    "phone": "+1234567890",
    "department": { "id": 2, "name": "Mathematics" },
    "designation": { "id": 1, "title": "Senior Teacher" },
    "joiningDate": "2023-08-01",
    "status": "ACTIVE"
  }
]
```

#### 1.2 Get Staff Profile Details
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff/{id}`
- **Response**: `200 OK` (Staff Object)

#### 1.3 Onboard New Staff Member
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/staff`
- **Request Body**:
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@school.edu",
  "phone": "+1987654321",
  "departmentId": 2,
  "designationId": 1,
  "gender": "FEMALE",
  "dateOfBirth": "1990-05-15",
  "joiningDate": "2026-09-01",
  "basicSalary": 45000.00
}
```
- **Response**: `200 OK` (Created Staff Object)

#### 1.4 Update Staff Details
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/staff/{id}`
- **Request Body**: Partial or full `StaffRequest` fields.
- **Response**: `200 OK`

#### 1.5 Deactivate Staff Member
- **URL**: `DELETE /api/staff/{id}`
- **Response**: `200 OK`

#### 1.6 Staff Key Metrics / Stats
- **URL**: `GET /api/staff/stats?schoolId={schoolId}`
- **Response**: `200 OK`
```json
{
  "totalStaff": 48,
  "activeStaff": 45,
  "onLeaveStaff": 3
}
```

#### 1.7 Get Staff Roles & Subject/Class Assignments
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff/{id}/assignments`
- **Response**: `200 OK`
```json
[
  {
    "id": 10,
    "userId": 1,
    "role": "TEACHER",
    "academicYearId": 1,
    "classId": 5,
    "className": "Grade 10 - A",
    "subjectId": 12,
    "subjectName": "Mathematics"
  }
]
```

#### 1.8 Assign Role / Subject / Class
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/staff/{id}/assignments`
- **Request Body**:
```json
{
  "role": "TEACHER",
  "academicYearId": 1,
  "classId": 5,
  "subjectId": 12
}
```
- **Response**: `200 OK`

#### 1.9 Revoke Assignment
- **URL**: `DELETE /api/v1/admin/schools/{schoolId}/hr/staff/{id}/assignments/{assignmentId}`
- **Response**: `204 No Content`

#### 1.10 Fetch Staff Documents
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff/{id}/documents`
- **Response**: `200 OK`
```json
[
  {
    "id": 12,
    "staffId": 1,
    "documentType": "DEGREE_CERTIFICATE",
    "title": "Master of Science Degree",
    "fileUrl": "https://storage.schooly.app/docs/staff-1-degree.pdf",
    "fileSize": 1048576,
    "mimeType": "application/pdf",
    "uploadedAt": "2026-09-20T10:30:00"
  }
]
```

#### 1.11 Upload Staff Document Metadata
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/staff/{id}/documents`
- **Request Body**:
```json
{
  "documentType": "PASSPORT",
  "title": "Passport Copy",
  "fileUrl": "https://storage.schooly.app/docs/passport.pdf",
  "fileSize": 524288,
  "mimeType": "application/pdf"
}
```
- **Response**: `200 OK` (StaffDocumentDTO)

#### 1.12 Delete Staff Document
- **URL**: `DELETE /api/v1/admin/schools/{schoolId}/hr/staff/{id}/documents/{docId}`
- **Response**: `204 No Content`

#### 1.13 Get Staff Bank Account Details
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff/{id}/bank-account`
- **Response**: `200 OK`
```json
{
  "id": 5,
  "staffId": 1,
  "bankName": "Chase Bank",
  "accountHolderName": "John Doe",
  "accountNumber": "987654321012",
  "ifscCode": "CHAS0001234",
  "branchName": "Downtown Branch",
  "isPrimary": true,
  "updatedAt": "2026-09-21T14:15:00"
}
```

#### 1.14 Save / Update Staff Bank Details
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/staff/{id}/bank-account`
- **Request Body**:
```json
{
  "bankName": "Chase Bank",
  "accountHolderName": "John Doe",
  "accountNumber": "987654321012",
  "ifscCode": "CHAS0001234",
  "branchName": "Downtown Branch",
  "isPrimary": true
}
```
- **Response**: `200 OK` (StaffBankAccountDTO)

---

### Category 2: School Departments

#### 2.1 List Departments
- **URL**: `GET /api/v1/hr/departments?schoolId={schoolId}`
- **Response**: `200 OK`
```json
{
  "status": "success",
  "message": "Fetched departments",
  "data": [
    {
      "id": 1,
      "schoolId": 1,
      "name": "Science",
      "code": "SCI",
      "description": "Science Department"
    }
  ]
}
```

#### 2.2 Create Department
- **URL**: `POST /api/v1/hr/departments?schoolId={schoolId}`
- **Request Body**:
```json
{
  "name": "Humanities",
  "code": "HUM",
  "description": "Arts and Social Sciences"
}
```
- **Response**: `200 OK`

#### 2.3 Update Department
- **URL**: `PUT /api/v1/hr/departments/{id}`
- **Request Body**: Department JSON
- **Response**: `200 OK`

#### 2.4 Delete Department
- **URL**: `DELETE /api/v1/hr/departments/{id}`
- **Response**: `200 OK`

---

### Category 3: School Designations

#### 3.1 List Designations
- **URL**: `GET /api/v1/hr/designations?schoolId={schoolId}`
- **Response**: `200 OK`
```json
{
  "status": "success",
  "message": "Fetched designations",
  "data": [
    {
      "id": 1,
      "title": "Principal",
      "code": "PRIN"
    },
    {
      "id": 2,
      "title": "Senior Lecturer",
      "code": "SNR_LEC"
    }
  ]
}
```

#### 3.2 Create Designation
- **URL**: `POST /api/v1/hr/designations?schoolId={schoolId}`
- **Request Body**:
```json
{
  "title": "Academic Coordinator",
  "code": "COORD"
}
```
- **Response**: `200 OK`

#### 3.3 Update Designation
- **URL**: `PUT /api/v1/hr/designations/{id}`
- **Response**: `200 OK`

#### 3.4 Delete Designation
- **URL**: `DELETE /api/v1/hr/designations/{id}`
- **Response**: `200 OK`

---

### Category 4: Staff Attendance

#### 4.1 Daily Attendance Register Grid
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/attendance/register?date=YYYY-MM-DD[&departmentId=X][&search=query]`
- **Response**: `200 OK`
```json
[
  {
    "id": 101,
    "schoolId": 1,
    "staffId": 1,
    "staffName": "John Doe",
    "staffCode": "STF-001",
    "departmentName": "Science",
    "date": "2026-09-26",
    "status": "PRESENT",
    "checkInTime": "08:15:00",
    "checkOutTime": "16:00:00",
    "remarks": "On time"
  }
]
```

#### 4.2 Attendance KPI Metrics
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/attendance/stats?date=YYYY-MM-DD`
- **Response**: `200 OK`
```json
{
  "date": "2026-09-26",
  "totalStaff": 50,
  "presentCount": 46,
  "absentCount": 2,
  "lateCount": 1,
  "halfDayCount": 0,
  "onLeaveCount": 1,
  "attendanceRate": 92.0
}
```

#### 4.3 Mark Single Staff Attendance
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/attendance`
- **Request Body**:
```json
{
  "staffId": 1,
  "date": "2026-09-26",
  "status": "PRESENT",
  "checkInTime": "08:20:00",
  "checkOutTime": "16:00:00",
  "remarks": "Regular shift"
}
```
- **Response**: `200 OK` (StaffAttendanceDTO)

#### 4.4 Bulk Submit Attendance for Department / School
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/attendance/bulk`
- **Request Body**:
```json
{
  "date": "2026-09-26",
  "attendances": [
    { "staffId": 1, "status": "PRESENT", "checkInTime": "08:00:00" },
    { "staffId": 2, "status": "LATE", "checkInTime": "08:45:00", "remarks": "Traffic delay" },
    { "staffId": 3, "status": "ABSENT" }
  ]
}
```
- **Response**: `200 OK` (List of StaffAttendanceDTO)

#### 4.5 Retrieve Attendance by Date
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/attendance?date=YYYY-MM-DD`
- **Response**: `200 OK`

#### 4.6 Monthly Attendance Breakdown for Individual Staff Member
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/attendance/staff/{staffId}?year=2026&month=9`
- **Response**: `200 OK`
```json
{
  "staffId": 1,
  "staffName": "John Doe",
  "staffCode": "STF-001",
  "departmentName": "Science",
  "designationName": "Senior Teacher",
  "month": 9,
  "year": 2026,
  "totalDaysInMonth": 30,
  "workingDays": 22,
  "presentDays": 20,
  "absentDays": 1,
  "lateDays": 1,
  "halfDays": 0,
  "leaveDays": 1,
  "attendancePercentage": 90.9,
  "records": [
    {
      "date": "2026-09-01",
      "status": "PRESENT",
      "checkInTime": "08:10:00",
      "checkOutTime": "16:05:00",
      "remarks": null
    }
  ]
}
```

#### 4.7 Exportable Monthly Staff Attendance Report
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/attendance/report?year=2026&month=9[&departmentId=X]`
- **Response**: `200 OK`
```json
{
  "month": 9,
  "year": 2026,
  "departmentId": 2,
  "departmentName": "Science",
  "totalStaffCount": 15,
  "averageAttendanceRate": 94.5,
  "staffSummaries": [
    {
      "staffId": 1,
      "staffName": "John Doe",
      "staffCode": "STF-001",
      "department": "Science",
      "designation": "Teacher",
      "present": 20,
      "absent": 1,
      "late": 1,
      "halfDay": 0,
      "leaves": 1,
      "attendanceRate": 90.9
    }
  ]
}
```

---

### Category 5: Staff Leave Management & Balances

#### 5.1 List All Leave Applications
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/leaves`
- **Response**: `200 OK` (List of Leave Applications)

#### 5.2 Submit Leave Application
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/leaves`
- **Request Body**:
```json
{
  "staffId": 1,
  "leaveTypeId": 2,
  "startDate": "2026-10-05",
  "endDate": "2026-10-07",
  "reason": "Family medical emergency"
}
```
- **Response**: `200 OK`

#### 5.3 Approve or Reject Leave Application
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/leaves/{leaveId}/approve`
- **Request Body**:
```json
{
  "status": "APPROVED",
  "approverComments": "Granted. Please arrange class substitutes."
}
```
- **Response**: `200 OK`

#### 5.4 List Configured Leave Types
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/leave-types`
- **Response**: `200 OK`
```json
[
  {
    "id": 1,
    "schoolId": 1,
    "name": "Casual Leave",
    "code": "CL",
    "daysAllowed": 12,
    "isPaid": true,
    "applicableGender": "ALL",
    "description": "Standard annual casual leave",
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "schoolId": 1,
    "name": "Maternity Leave",
    "code": "ML",
    "daysAllowed": 90,
    "isPaid": true,
    "applicableGender": "FEMALE",
    "description": "Paid maternity leave",
    "status": "ACTIVE"
  }
]
```

#### 5.5 Create Leave Type
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/leave-types`
- **Request Body**:
```json
{
  "name": "Sick Leave",
  "code": "SL",
  "daysAllowed": 10,
  "isPaid": true,
  "applicableGender": "ALL",
  "description": "Medical leave with doctor certificate"
}
```
- **Response**: `200 OK` (SchoolLeaveTypeDTO)

#### 5.6 Edit Leave Type
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/leave-types/{id}`
- **Request Body**: Same as Create
- **Response**: `200 OK`

#### 5.7 Delete Leave Type
- **URL**: `DELETE /api/v1/admin/schools/{schoolId}/hr/leave-types/{id}`
- **Response**: `204 No Content`

#### 5.8 Fetch Staff Leave Balances
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/staff/{staffId}/leave-balances`
- **Response**: `200 OK`
```json
[
  {
    "id": 10,
    "schoolId": 1,
    "staffId": 1,
    "leaveTypeId": 1,
    "leaveTypeName": "Casual Leave",
    "leaveTypeCode": "CL",
    "allocatedDays": 12,
    "usedDays": 4,
    "remainingDays": 8,
    "academicYearId": 1
  }
]
```

#### 5.9 Allocate / Adjust Annual Leave Quota
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/staff/{staffId}/leave-balances`
- **Request Body**:
```json
{
  "leaveTypeId": 1,
  "allocatedDays": 14,
  "usedDays": 4,
  "academicYearId": 1
}
```
- **Response**: `200 OK` (StaffLeaveBalanceDTO)

---

### Category 6: Recruitment & Candidate Tracking

#### 6.1 List Applicants
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates?[role=...][&status=...][&search=...]`
- **Query Filters**:
  - `role`: e.g. "Math Teacher"
  - `status`: `NEW`, `SCREENING`, `INTERVIEW_SCHEDULED`, `INTERVIEWED`, `OFFERED`, `HIRED`, `REJECTED`
  - `search`: Name or email search
- **Response**: `200 OK`
```json
[
  {
    "id": 4,
    "schoolId": 1,
    "firstName": "Robert",
    "lastName": "Johnson",
    "email": "robert.j@example.com",
    "phone": "+1555234567",
    "roleApplied": "High School Physics Teacher",
    "departmentId": 2,
    "departmentName": "Science",
    "status": "INTERVIEW_SCHEDULED",
    "resumeUrl": "https://storage.schooly.app/resumes/r-johnson.pdf",
    "notes": "Strong laboratory background and 5 years AP Physics teaching experience",
    "rating": 4,
    "interviewDate": "2026-09-30T10:00:00",
    "createdAt": "2026-09-15T08:00:00",
    "updatedAt": "2026-09-24T11:30:00"
  }
]
```

#### 6.2 Get Candidate Profile & Resume
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates/{id}`
- **Response**: `200 OK` (RecruitmentCandidateDTO)

#### 6.3 Add Candidate Application
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates`
- **Request Body**:
```json
{
  "firstName": "Emily",
  "lastName": "Clark",
  "email": "emily.clark@example.com",
  "phone": "+1555987123",
  "roleApplied": "Primary English Teacher",
  "departmentId": 3,
  "resumeUrl": "https://storage.schooly.app/resumes/emily.pdf",
  "notes": "Certified ESL instructor",
  "rating": 5
}
```
- **Response**: `200 OK`

#### 6.4 Update Candidate Details & Notes
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates/{id}`
- **Response**: `200 OK`

#### 6.5 Advance Candidate Pipeline Status
- **URL**: `PATCH /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates/{id}/status`
- **Request Body**:
```json
{
  "status": "OFFERED"
}
```
*(Query parameter `?status=OFFERED` is also supported)*
- **Response**: `200 OK` (Updated RecruitmentCandidateDTO)

#### 6.6 1-Click Hire (Convert Candidate into Active Staff)
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates/{id}/convert-to-staff`
- **Description**: Transitions candidate status to `HIRED`, creates an active record in `staff` with generated `staff_code`, email, department link, and default balance structures.
- **Response**: `200 OK` (Newly created Staff Object)

#### 6.7 Delete Candidate Record
- **URL**: `DELETE /api/v1/admin/schools/{schoolId}/hr/recruitment/candidates/{id}`
- **Response**: `204 No Content`

---

### Category 7: Staff Tasks

#### 7.1 List Staff Tasks
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/tasks?[staffId=X][&status=...][&priority=...]`
- **Filters**:
  - `staffId`: ID of assigned staff
  - `status`: `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
  - `priority`: `LOW`, `MEDIUM`, `HIGH`, `URGENT`
- **Response**: `200 OK`
```json
[
  {
    "id": 8,
    "schoolId": 1,
    "title": "Submit Q3 Science Curriculum Plan",
    "description": "Prepare detailed syllabus and experiment materials for Term 3",
    "assignedToStaffId": 1,
    "assignedToStaffName": "John Doe",
    "assignedByStaffId": 5,
    "assignedByStaffName": "Principal Williams",
    "dueDate": "2026-10-15",
    "priority": "HIGH",
    "status": "IN_PROGRESS",
    "completedAt": null,
    "createdAt": "2026-09-22T09:00:00",
    "updatedAt": "2026-09-24T14:30:00"
  }
]
```

#### 7.2 Get Task Details
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/tasks/{id}`
- **Response**: `200 OK` (StaffTaskDTO)

#### 7.3 Assign Task to Staff
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/tasks`
- **Request Body**:
```json
{
  "title": "Grade 10 Lab Safety Audit",
  "description": "Inspect chemical storage and emergency eye wash stations",
  "assignedToStaffId": 1,
  "dueDate": "2026-10-05",
  "priority": "URGENT"
}
```
- **Response**: `200 OK` (StaffTaskDTO)

#### 7.4 Update Task Details
- **URL**: `PUT /api/v1/admin/schools/{schoolId}/hr/tasks/{id}`
- **Request Body**: Full `StaffTaskRequest`
- **Response**: `200 OK`

#### 7.5 Mark Task Status (Pending, In Progress, Completed)
- **URL**: `PATCH /api/v1/admin/schools/{schoolId}/hr/tasks/{id}/status`
- **Request Body**:
```json
{
  "status": "COMPLETED"
}
```
- **Response**: `200 OK`

#### 7.6 Delete Task
- **URL**: `DELETE /api/v1/admin/schools/{schoolId}/hr/tasks/{id}`
- **Response**: `204 No Content`

---

### Category 8: Payroll, Salary Slips & Advances

#### 8.1 List Payroll Entries
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/payroll`
- **Response**: `200 OK` (List of StaffPayrollDTO)

#### 8.2 Calculate Single Staff Payroll
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/payroll`
- **Request Body**:
```json
{
  "staffId": 1,
  "payrollMonth": "September",
  "payrollYear": 2026,
  "basicSalary": 4000.00,
  "allowances": 600.00,
  "deductions": 250.00,
  "paymentMode": "BANK_TRANSFER"
}
```
- **Response**: `200 OK` (StaffPayrollDTO)

#### 8.3 Run Automated Monthly Payroll for All Active Staff (Batch)
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/payroll/generate-batch`
- **UI Trigger**: "Generate Monthly Payroll" button in `PayrollManagement.tsx`
- **Request Body**:
```json
{
  "payrollMonth": "September",
  "payrollYear": 2026
}
```
*(Also supports `?payrollMonth=September&payrollYear=2026`)*
- **Response**: `200 OK`
```json
[
  {
    "id": 101,
    "staffId": 1,
    "staffName": "John Doe",
    "staffCode": "STF-001",
    "designation": "Senior Teacher",
    "department": "Science",
    "payrollMonth": "September",
    "payrollYear": 2026,
    "basicSalary": 4000.00,
    "allowances": 400.00,
    "deductions": 200.00,
    "netSalary": 4200.00,
    "paymentStatus": "PENDING"
  }
]
```

#### 8.4 Payroll Summary & Payout KPIs
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/payroll/summary?[month=September][&year=2026]`
- **Response**: `200 OK`
```json
{
  "month": "September",
  "year": 2026,
  "totalStaffCount": 50,
  "paidStaffCount": 42,
  "pendingStaffCount": 8,
  "totalNetPayout": 210000.00,
  "totalAllowances": 18500.00,
  "totalDeductions": 9200.00,
  "currency": "USD"
}
```

#### 8.5 Generate / Fetch Downloadable Salary Slip
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/payroll/{id}/payslip`
- **Response**: `200 OK`
```json
{
  "payrollId": 101,
  "schoolId": 1,
  "schoolName": "Oakridge International School",
  "schoolAddress": "123 Academic Way, Metropolis",
  "staffId": 1,
  "staffName": "John Doe",
  "staffCode": "STF-001",
  "designation": "Senior Teacher",
  "department": "Science",
  "bankName": "Chase Bank",
  "accountNumber": "987654321012",
  "ifscCode": "CHAS0001234",
  "payrollMonth": "September",
  "payrollYear": 2026,
  "basicSalary": 4000.00,
  "grossSalary": 4400.00,
  "totalAllowances": 400.00,
  "totalDeductions": 200.00,
  "netSalary": 4200.00,
  "paymentStatus": "PAID",
  "paymentDate": "2026-09-30",
  "allowanceBreakdown": [
    { "name": "Housing Allowance", "amount": 250.00 },
    { "name": "Transport Allowance", "amount": 150.00 }
  ],
  "deductionBreakdown": [
    { "name": "Income Tax", "amount": 150.00 },
    { "name": "Health Insurance", "amount": 50.00 }
  ],
  "generatedAt": "2026-09-26T18:30:00"
}
```

#### 8.6 Transition Payroll Status
- **URL**: `PATCH /api/v1/admin/schools/{schoolId}/hr/payroll/{id}/status`
- **Request Body**:
```json
{
  "status": "PAID",
  "paymentDate": "2026-09-30"
}
```
*(Query parameters `?status=PAID&paymentDate=2026-09-30` also supported)*
- **Response**: `200 OK`

#### 8.7 List Salary Advance Requests
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/advances?[staffId=X][&status=...]`
- **Response**: `200 OK`
```json
[
  {
    "id": 12,
    "schoolId": 1,
    "staffId": 1,
    "staffName": "John Doe",
    "staffCode": "STF-001",
    "amount": 1000.00,
    "reason": "Home relocation expenses",
    "requestedDate": "2026-09-10",
    "approvalDate": "2026-09-12",
    "approvedBy": "Finance Admin",
    "status": "APPROVED",
    "recoveredAmount": 500.00,
    "recoveryMonth": "October 2026"
  }
]
```

#### 8.8 Issue Salary Advance
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/advances`
- **Request Body**:
```json
{
  "staffId": 1,
  "amount": 1200.00,
  "reason": "Medical expense advance",
  "recoveryMonth": "November 2026"
}
```
- **Response**: `200 OK` (StaffAdvanceDTO)

#### 8.9 Approve / Reject Salary Advance
- **URL**: `PATCH /api/v1/admin/schools/{schoolId}/hr/advances/{id}/status`
- **Request Body**:
```json
{
  "status": "APPROVED",
  "recoveredAmount": 0.00
}
```
- **Response**: `200 OK`

---

### Category 9: Staff Certificates & ID Cards

#### 9.1 List Issued Staff Certificates
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/certificates?[staffId=X][&certificateType=...][&status=...]`
- **Supported Types**: `EXPERIENCE`, `JOINING`, `RELIEVING`, `NOC`
- **Response**: `200 OK`
```json
[
  {
    "id": 14,
    "schoolId": 1,
    "staffId": 1,
    "staffName": "John Doe",
    "staffCode": "STF-001",
    "departmentName": "Science",
    "designationName": "Senior Teacher",
    "certificateType": "EXPERIENCE",
    "certificateNumber": "CERT-2026-0014",
    "issueDate": "2026-09-25",
    "validUntil": null,
    "fileUrl": "https://storage.schooly.app/certificates/cert-14.pdf",
    "status": "ISSUED",
    "issuedBy": "HR Director"
  }
]
```

#### 9.2 Generate & Issue Staff Certificate
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/certificates/generate`
- **Request Body**:
```json
{
  "staffId": 1,
  "certificateType": "EXPERIENCE",
  "issueDate": "2026-09-26",
  "reason": "Applying for higher education accreditation",
  "customFields": {
    "conduct": "Exemplary",
    "responsibilities": "Head of Physics Laboratory and AP Instructor"
  }
}
```
- **Response**: `200 OK` (StaffCertificateDTO)

#### 9.3 List Generated Staff ID Card Batches
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/id-cards`
- **Response**: `200 OK`
```json
[
  {
    "id": 22,
    "schoolId": 1,
    "staffId": 1,
    "staffName": "John Doe",
    "staffCode": "STF-001",
    "designationName": "Senior Teacher",
    "departmentName": "Science",
    "bloodGroup": "O+",
    "emergencyContact": "+1555333444",
    "photoUrl": "https://storage.schooly.app/photos/stf-001.png",
    "cardNumber": "IDC-2026-0001",
    "issueDate": "2026-09-01",
    "expiryDate": "2028-08-31",
    "status": "ACTIVE"
  }
]
```

#### 9.4 Batch Render / Generate Staff ID Cards
- **URL**: `POST /api/v1/admin/schools/{schoolId}/hr/id-cards/generate`
- **Request Body**:
```json
{
  "departmentId": 2,
  "staffIds": [1, 2, 3],
  "validityYears": 2
}
```
*(If `staffIds` is empty, generates for all active staff in the department or school)*
- **Response**: `200 OK` (List of generated StaffIdCardDTO)

---

### Category 10: Teacher Workload & Substitution

#### 10.1 Workload & Substitution Analytics
- **URL**: `GET /api/v1/admin/schools/{schoolId}/hr/workload/analytics?[academicYearId=X][&departmentId=Y]`
- **Backing View**: `TeacherWorkload.tsx`
- **Response**: `200 OK`
```json
{
  "totalTeachers": 24,
  "averageWeeklyHours": 22.5,
  "utilizationPercentage": 75.0,
  "overloadedTeachersCount": 3,
  "underutilizedTeachersCount": 4,
  "teacherWorkloads": [
    {
      "teacherId": 1,
      "teacherName": "John Doe",
      "teacherCode": "STF-001",
      "department": "Science",
      "assignedWeeklyLectures": 24,
      "maxWeeklyLectures": 30,
      "freePeriods": 6,
      "utilizationRate": 80.0,
      "status": "BALANCED",
      "subjectNames": ["Physics 101", "Advanced Mechanics"],
      "classNames": ["Grade 10 - A", "Grade 11 - B"]
    }
  ],
  "weeklyDaySlotMatrix": {
    "MONDAY": {
      "slot-1": ["John Doe", "Sarah Connor"],
      "slot-2": ["John Doe"]
    },
    "TUESDAY": {
      "slot-1": ["Mark Lee"]
    }
  },
  "substitutionGaps": [
    {
      "date": "2026-09-26",
      "dayOfWeek": "FRIDAY",
      "absentTeacherId": 2,
      "absentTeacherName": "Sarah Connor",
      "periodName": "Period 3",
      "startTime": "10:30:00",
      "endTime": "11:15:00",
      "className": "Grade 9 - B",
      "subjectName": "Biology",
      "suggestedSubstitutes": [
        {
          "teacherId": 1,
          "name": "John Doe",
          "department": "Science",
          "freePeriodsToday": 3,
          "currentDayLoad": 2
        }
      ]
    }
  ]
}
```

---

## 3. Copy-Paste TypeScript Interfaces (`hr.types.ts`)

Save this in `schooly-web/src/types/hr.ts` or `src/lib/types/hr.types.ts`:

```typescript
// ==========================================
// 1. Staff Profile & Documents
// ==========================================
export interface StaffDocumentDTO {
  id: number;
  staffId: number;
  documentType: string;
  title: string;
  fileUrl: string;
  fileSize?: number;
  mimeType?: string;
  uploadedAt: string;
}

export interface StaffDocumentRequest {
  documentType: string;
  title: string;
  fileUrl: string;
  fileSize?: number;
  mimeType?: string;
}

export interface StaffBankAccountDTO {
  id: number;
  staffId: number;
  bankName: string;
  accountHolderName: string;
  accountNumber: string;
  ifscCode: string;
  branchName?: string;
  isPrimary?: boolean;
  updatedAt: string;
}

export interface StaffBankAccountRequest {
  bankName: string;
  accountHolderName: string;
  accountNumber: string;
  ifscCode: string;
  branchName?: string;
  isPrimary?: boolean;
}

// ==========================================
// 2. Attendance & Reports
// ==========================================
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'HALF_DAY' | 'ON_LEAVE';

export interface StaffAttendanceDTO {
  id: number;
  schoolId: number;
  staffId: number;
  staffName: string;
  staffCode: string;
  departmentName?: string;
  date: string;
  status: AttendanceStatus;
  checkInTime?: string;
  checkOutTime?: string;
  remarks?: string;
}

export interface StaffAttendanceStatsDTO {
  date: string;
  totalStaff: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  halfDayCount: number;
  onLeaveCount: number;
  attendanceRate: number;
}

export interface StaffMonthlyAttendanceBreakdownDTO {
  staffId: number;
  staffName: string;
  staffCode: string;
  departmentName: string;
  designationName: string;
  month: number;
  year: number;
  totalDaysInMonth: number;
  workingDays: number;
  presentDays: number;
  absentDays: number;
  lateDays: number;
  halfDays: number;
  leaveDays: number;
  attendancePercentage: number;
  records: Array<{
    date: string;
    status: AttendanceStatus;
    checkInTime?: string;
    checkOutTime?: string;
    remarks?: string;
  }>;
}

export interface StaffAttendanceReportDTO {
  month: number;
  year: number;
  departmentId?: number;
  departmentName?: string;
  totalStaffCount: number;
  averageAttendanceRate: number;
  staffSummaries: Array<{
    staffId: number;
    staffName: string;
    staffCode: string;
    department: string;
    designation: string;
    present: number;
    absent: number;
    late: number;
    halfDay: number;
    leaves: number;
    attendanceRate: number;
  }>;
}

// ==========================================
// 3. Leave Types & Quotas
// ==========================================
export interface SchoolLeaveTypeDTO {
  id: number;
  schoolId: number;
  name: string;
  code: string;
  daysAllowed: number;
  isPaid: boolean;
  applicableGender?: 'ALL' | 'MALE' | 'FEMALE';
  description?: string;
  status?: string;
}

export interface StaffLeaveBalanceDTO {
  id: number;
  schoolId: number;
  staffId: number;
  leaveTypeId: number;
  leaveTypeName: string;
  leaveTypeCode: string;
  allocatedDays: number;
  usedDays: number;
  remainingDays: number;
  academicYearId?: number;
}

// ==========================================
// 4. Recruitment Pipeline
// ==========================================
export type CandidateStatus =
  | 'NEW'
  | 'SCREENING'
  | 'INTERVIEW_SCHEDULED'
  | 'INTERVIEWED'
  | 'OFFERED'
  | 'HIRED'
  | 'REJECTED';

export interface RecruitmentCandidateDTO {
  id: number;
  schoolId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  roleApplied: string;
  departmentId?: number;
  departmentName?: string;
  status: CandidateStatus;
  resumeUrl?: string;
  notes?: string;
  rating?: number;
  interviewDate?: string;
  createdAt: string;
  updatedAt: string;
}

// ==========================================
// 5. Staff Tasks
// ==========================================
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type TaskStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface StaffTaskDTO {
  id: number;
  schoolId: number;
  title: string;
  description?: string;
  assignedToStaffId: number;
  assignedToStaffName: string;
  assignedByStaffId?: number;
  assignedByStaffName?: string;
  dueDate?: string;
  priority: TaskPriority;
  status: TaskStatus;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// ==========================================
// 6. Payroll & Advances
// ==========================================
export interface StaffPayrollDTO {
  id: number;
  schoolId: number;
  staffId: number;
  staffName: string;
  staffCode: string;
  designation?: string;
  department?: string;
  payrollMonth: string;
  payrollYear: number;
  basicSalary: number;
  allowances: number;
  deductions: number;
  netSalary: number;
  paymentStatus: 'PENDING' | 'APPROVED' | 'PAID';
  paymentDate?: string;
  paymentMode?: string;
  transactionReference?: string;
  createdAt: string;
}

export interface StaffPayrollSummaryDTO {
  month?: string;
  year?: number;
  totalStaffCount: number;
  paidStaffCount: number;
  pendingStaffCount: number;
  totalNetPayout: number;
  totalAllowances: number;
  totalDeductions: number;
  currency: string;
}

export interface StaffPayslipDTO {
  payrollId: number;
  schoolId: number;
  schoolName?: string;
  schoolAddress?: string;
  staffId: number;
  staffName: string;
  staffCode: string;
  designation?: string;
  department?: string;
  bankName?: string;
  accountNumber?: string;
  ifscCode?: string;
  payrollMonth: string;
  payrollYear: number;
  basicSalary: number;
  grossSalary: number;
  totalAllowances: number;
  totalDeductions: number;
  netSalary: number;
  paymentStatus: string;
  paymentDate?: string;
  allowanceBreakdown?: Array<{ name: string; amount: number }>;
  deductionBreakdown?: Array<{ name: string; amount: number }>;
  generatedAt: string;
}

export interface StaffAdvanceDTO {
  id: number;
  schoolId: number;
  staffId: number;
  staffName: string;
  staffCode: string;
  amount: number;
  reason?: string;
  requestedDate: string;
  approvalDate?: string;
  approvedBy?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'RECOVERED';
  recoveredAmount?: number;
  recoveryMonth?: string;
}

// ==========================================
// 7. Certificates & ID Cards
// ==========================================
export interface StaffCertificateDTO {
  id: number;
  schoolId: number;
  staffId: number;
  staffName: string;
  staffCode: string;
  departmentName?: string;
  designationName?: string;
  certificateType: 'EXPERIENCE' | 'JOINING' | 'RELIEVING' | 'NOC';
  certificateNumber: string;
  issueDate: string;
  validUntil?: string;
  fileUrl?: string;
  status: string;
  issuedBy?: string;
  createdAt: string;
}

export interface StaffIdCardDTO {
  id: number;
  schoolId: number;
  staffId: number;
  staffName: string;
  staffCode: string;
  designationName?: string;
  departmentName?: string;
  bloodGroup?: string;
  emergencyContact?: string;
  photoUrl?: string;
  cardNumber: string;
  issueDate: string;
  expiryDate?: string;
  status: string;
  generatedAt: string;
}

// ==========================================
// 8. Workload & Substitution Analytics
// ==========================================
export interface TeacherWorkloadAnalyticsDTO {
  totalTeachers: number;
  averageWeeklyHours: number;
  utilizationPercentage: number;
  overloadedTeachersCount: number;
  underutilizedTeachersCount: number;
  teacherWorkloads: Array<{
    teacherId: number;
    teacherName: string;
    teacherCode: string;
    department: string;
    assignedWeeklyLectures: number;
    maxWeeklyLectures: number;
    freePeriods: number;
    utilizationRate: number;
    status: 'UNDERUTILIZED' | 'BALANCED' | 'OVERLOADED';
    subjectNames: string[];
    classNames: string[];
  }>;
  weeklyDaySlotMatrix: Record<string, Record<string, string[]>>;
  substitutionGaps: Array<{
    date: string;
    dayOfWeek: string;
    absentTeacherId: number;
    absentTeacherName: string;
    periodName: string;
    startTime: string;
    endTime: string;
    className: string;
    subjectName: string;
    suggestedSubstitutes: Array<{
      teacherId: number;
      name: string;
      department: string;
      freePeriodsToday: number;
      currentDayLoad: number;
    }>;
  }>;
}
```

---

## 4. Frontend Integration Snippets (`hr.service.ts`)

```typescript
import axios from 'axios';
import type {
  StaffDocumentDTO,
  StaffBankAccountDTO,
  StaffMonthlyAttendanceBreakdownDTO,
  StaffAttendanceReportDTO,
  SchoolLeaveTypeDTO,
  StaffLeaveBalanceDTO,
  RecruitmentCandidateDTO,
  StaffTaskDTO,
  StaffPayrollDTO,
  StaffPayrollSummaryDTO,
  StaffPayslipDTO,
  StaffAdvanceDTO,
  StaffCertificateDTO,
  StaffIdCardDTO,
  TeacherWorkloadAnalyticsDTO,
} from './hr.types';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const hrService = {
  // Staff Documents & Bank
  getStaffDocuments: (schoolId: number, staffId: number) =>
    api.get<StaffDocumentDTO[]>(`/admin/schools/${schoolId}/hr/staff/${staffId}/documents`).then((r) => r.data),

  addStaffDocument: (schoolId: number, staffId: number, data: any) =>
    api.post<StaffDocumentDTO>(`/admin/schools/${schoolId}/hr/staff/${staffId}/documents`, data).then((r) => r.data),

  getStaffBankAccount: (schoolId: number, staffId: number) =>
    api.get<StaffBankAccountDTO>(`/admin/schools/${schoolId}/hr/staff/${staffId}/bank-account`).then((r) => r.data),

  updateStaffBankAccount: (schoolId: number, staffId: number, data: any) =>
    api.put<StaffBankAccountDTO>(`/admin/schools/${schoolId}/hr/staff/${staffId}/bank-account`, data).then((r) => r.data),

  // Attendance Reports
  getStaffMonthlyAttendance: (schoolId: number, staffId: number, year: number, month: number) =>
    api.get<StaffMonthlyAttendanceBreakdownDTO>(`/admin/schools/${schoolId}/hr/attendance/staff/${staffId}`, {
      params: { year, month },
    }).then((r) => r.data),

  getMonthlyAttendanceReport: (schoolId: number, year: number, month: number, departmentId?: number) =>
    api.get<StaffAttendanceReportDTO>(`/admin/schools/${schoolId}/hr/attendance/report`, {
      params: { year, month, departmentId },
    }).then((r) => r.data),

  // Leave Types & Balances
  getLeaveTypes: (schoolId: number) =>
    api.get<SchoolLeaveTypeDTO[]>(`/admin/schools/${schoolId}/hr/leave-types`).then((r) => r.data),

  getStaffLeaveBalances: (schoolId: number, staffId: number) =>
    api.get<StaffLeaveBalanceDTO[]>(`/admin/schools/${schoolId}/hr/staff/${staffId}/leave-balances`).then((r) => r.data),

  // Recruitment
  getCandidates: (schoolId: number, params?: { role?: string; status?: string; search?: string }) =>
    api.get<RecruitmentCandidateDTO[]>(`/admin/schools/${schoolId}/hr/recruitment/candidates`, { params }).then((r) => r.data),

  updateCandidateStatus: (schoolId: number, id: number, status: string) =>
    api.patch<RecruitmentCandidateDTO>(`/admin/schools/${schoolId}/hr/recruitment/candidates/${id}/status`, { status }).then((r) => r.data),

  convertCandidateToStaff: (schoolId: number, id: number) =>
    api.post(`/admin/schools/${schoolId}/hr/recruitment/candidates/${id}/convert-to-staff`).then((r) => r.data),

  // Tasks
  getTasks: (schoolId: number, params?: { staffId?: number; status?: string; priority?: string }) =>
    api.get<StaffTaskDTO[]>(`/admin/schools/${schoolId}/hr/tasks`, { params }).then((r) => r.data),

  updateTaskStatus: (schoolId: number, id: number, status: string) =>
    api.patch<StaffTaskDTO>(`/admin/schools/${schoolId}/hr/tasks/${id}/status`, { status }).then((r) => r.data),

  // Payroll
  generateBatchPayroll: (schoolId: number, payrollMonth: string, payrollYear: number) =>
    api.post<StaffPayrollDTO[]>(`/admin/schools/${schoolId}/hr/payroll/generate-batch`, { payrollMonth, payrollYear }).then((r) => r.data),

  getPayrollSummary: (schoolId: number, month?: string, year?: number) =>
    api.get<StaffPayrollSummaryDTO>(`/admin/schools/${schoolId}/hr/payroll/summary`, { params: { month, year } }).then((r) => r.data),

  getPayslip: (schoolId: number, payrollId: number) =>
    api.get<StaffPayslipDTO>(`/admin/schools/${schoolId}/hr/payroll/${payrollId}/payslip`).then((r) => r.data),

  updatePayrollStatus: (schoolId: number, id: number, status: string, paymentDate?: string) =>
    api.patch<StaffPayrollDTO>(`/admin/schools/${schoolId}/hr/payroll/${id}/status`, { status, paymentDate }).then((r) => r.data),

  // Certificates & ID Cards
  generateCertificate: (schoolId: number, data: any) =>
    api.post<StaffCertificateDTO>(`/admin/schools/${schoolId}/hr/certificates/generate`, data).then((r) => r.data),

  generateIdCards: (schoolId: number, data: { departmentId?: number; staffIds?: number[]; validityYears?: number }) =>
    api.post<StaffIdCardDTO[]>(`/admin/schools/${schoolId}/hr/id-cards/generate`, data).then((r) => r.data),

  // Teacher Workload & Substitution
  getWorkloadAnalytics: (schoolId: number, academicYearId?: number, departmentId?: number) =>
    api.get<TeacherWorkloadAnalyticsDTO>(`/admin/schools/${schoolId}/hr/workload/analytics`, {
      params: { academicYearId, departmentId },
    }).then((r) => r.data),
};
```
