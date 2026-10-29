# Schooly ERP — Complete Notification Triggers & Role Matrix Guide

All notifications in Schooly ERP are persisted in the `notifications` table (`V153__create_notifications_system.sql`) and scoped by `school_id`, `user_id`, and `target_role` (`SUPER_ADMIN`, `ADMIN`, `TEACHER`, `PARENT`, `STUDENT`, `STAFF`).

---

## 1. Architecture & Recipient Resolution Flow

- **`notifyStudentAndParents(...)`**: Resolves the student's `userId` (`targetRole = STUDENT`) and all linked parents via `StudentParentRepository` (with fallback matching by guardian phone/email in `users`, `targetRole = PARENT`).
- **`notifyClassAndSection(...)`**: Resolves all active students, their parents, and assigned class/subject teachers for a given class and optional section.
- **`notifyClassTeachersOfStudent(...)`**: Resolves assigned class teachers from both `ClassTeacherAssignment` and centralized `UserAssignment` records.
- **`notifyStaffMember(...)`**: Resolves the staff member's `userId` and dynamically assigns `TEACHER` or `STAFF` as `targetRole` via `UserSchoolRoleRepository`.
- **`notifySchoolAdmins(...)`**: Broadcasts to all active `ADMIN` users in the school.

---

## 2. Complete Event-to-Notification Trigger Catalog

### 2.1 Academic — Homework & Assignments (`HomeworkService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Teacher/Admin assigns new homework to a class/section (when status is not `DRAFT`) | `createHomework` | `HOMEWORK_ASSIGNED` | `ACADEMIC` | `NORMAL` | `STUDENT` (all active students in class/section)<br>`PARENT` (all linked parents) | `/homework` |
| Student submits homework solution | `submitHomework` | `HOMEWORK_SUBMITTED` | `ACADEMIC` | `NORMAL` | `TEACHER` (assigning teacher; falls back to class teachers of the student) | `/admin/homework/submissions` |
| Teacher grades / evaluates a student's homework submission | `gradeSubmission` | `HOMEWORK_GRADED` | `ACADEMIC` | `NORMAL` | `STUDENT` (submitting student)<br>`PARENT` (linked parents) | `/homework` |

---

### 2.2 Attendance — Daily & Subject-Wise Attendance (`AttendanceService`, `SubjectAttendanceService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Student marked **Absent** in daily or bulk attendance | `saveBulkAttendance`, `createAttendance` | `ATTENDANCE_ABSENT` | `ATTENDANCE` | `HIGH` | `STUDENT`, `PARENT` | `/attendance` |
| Student marked **Late** in daily or bulk attendance | `saveBulkAttendance`, `createAttendance` | `ATTENDANCE_LATE` | `ATTENDANCE` | `NORMAL` | `STUDENT`, `PARENT` | `/attendance` |
| Student marked **Absent** in a specific subject period | `saveBulkSubjectAttendance`, `createSubjectAttendance` | `ATTENDANCE_ABSENT` | `ATTENDANCE` | `HIGH` | `STUDENT`, `PARENT` | `/attendance` |
| Student marked **Late** in a specific subject period | `saveBulkSubjectAttendance`, `createSubjectAttendance` | `ATTENDANCE_LATE` | `ATTENDANCE` | `NORMAL` | `STUDENT`, `PARENT` | `/attendance` |

---

### 2.3 Examinations, Marks, Admit Cards & Report Cards (`ExamScheduleService`, `ExamAdmitCardService`, `ExamReportCardService`, `ExamMarkService`, `ExamTeacherRemarkService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Exam schedule created (single or bulk) for a class/section | `createSchedule`, `createBulkSchedules` | `EXAM_SCHEDULE_PUBLISHED` | `ACADEMIC` | `NORMAL` | `STUDENT`, `PARENT`, `TEACHER` | `/exams/schedule` |
| Admit cards generated for students / class | `generateAdmitCards` | `EXAM_ADMIT_CARD_READY` | `ACADEMIC` | `HIGH` | `STUDENT`, `PARENT` | `/exams/admit-cards` |
| Admit cards officially released for an exam setup | `releaseAllCards` | `EXAM_ADMIT_CARD_READY` | `ACADEMIC` | `HIGH` | `STUDENT`, `PARENT` | `/exams/admit-cards` |
| Subject marks entered/updated for a student | `saveMark` | `EXAM_MARKS_ENTERED` | `ACADEMIC` | `NORMAL` | `STUDENT`, `PARENT` | `/exams/report-cards` |
| Bulk subject marks submitted for a class | `saveBulkMarks` | `EXAM_MARKS_ENTERED` | `ACADEMIC` | `LOW` | `ADMIN` (summary) + per-student `STUDENT`, `PARENT` | `/exams/marks` |
| Report card batch generated & published | `generateReportCards` | `EXAM_RESULT_PUBLISHED` | `ACADEMIC` | `HIGH` | `STUDENT`, `PARENT`, `TEACHER` | `/exams/report-cards` |
| Class teacher saves term remark for a student | `saveRemark` | `TEACHER_REMARK_ADDED` | `ACADEMIC` | `NORMAL` | `STUDENT`, `PARENT` | `/exams/report-cards` |

---

### 2.4 Finance & Fee Management (`FeeDueServiceImpl`, `FeePaymentServiceImpl`, `TeacherFeeServiceImpl`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Batch fee generation confirmed for a class | `confirmBatchGeneration` | `FEE_INVOICE_GENERATED` | `FINANCE` | `NORMAL` | `STUDENT`, `PARENT` | `/finance/dues` |
| Ad-hoc fee assigned to an individual student | `assignAdHocFee` | `FEE_INVOICE_GENERATED` | `FINANCE` | `NORMAL` | `STUDENT`, `PARENT` | `/finance/dues` |
| Fee discount applied to a student's fee due | `applyDiscount` | `FEE_DISCOUNT_APPLIED` | `FINANCE` | `NORMAL` | `STUDENT`, `PARENT` | `/finance/dues` |
| Fee payment processed (online or counter) | `processPayment` | `FEE_PAYMENT_SUCCESS` | `FINANCE` | `NORMAL` (`STUDENT`/`PARENT`), `LOW` (`ADMIN`) | `STUDENT`, `PARENT`, `ADMIN` | `/finance/payments` |
| Pending fee reminders sent by Class Teacher or Admin | `sendReminders` | `FEE_DUE_REMINDER` | `FINANCE` | `HIGH` | `STUDENT`, `PARENT` | `/finance/dues` |

---

### 2.5 Human Resources (`StaffLeaveService`, `StaffPayrollService`, `StaffTaskService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Teacher or Staff member applies for leave | `applyLeave` | `LEAVE_REQUESTED` | `HR` | `HIGH` | `ADMIN` | `/hr/leaves` |
| Admin approves or rejects staff leave request | `approveLeave` | `LEAVE_STATUS_UPDATED` | `HR` | `HIGH` | `TEACHER` or `STAFF` | `/hr/leaves` |
| Single or batch monthly payroll generated | `runPayroll`, `generateBatchPayroll` | `PAYROLL_GENERATED` | `HR` | `NORMAL` | `TEACHER` or `STAFF` | `/hr/payroll` |
| Payroll status marked as `PAID` (salary disbursed) | `updatePayrollStatus` | `PAYROLL_PAID` | `HR` | `HIGH` | `TEACHER` or `STAFF` | `/hr/payroll` |
| New task assigned to a staff member | `createTask` | `STAFF_TASK_ASSIGNED` | `HR` | `HIGH` / `NORMAL` | `TEACHER` or `STAFF` | `/hr/tasks` |
| Staff member marks assigned task as `COMPLETED` | `updateTaskStatus` | `STAFF_TASK_UPDATED` | `HR` | `LOW` | `ADMIN` | `/hr/tasks` |

---

### 2.6 Student Leave & Online Admissions (`StudentLeaveService`, `OnlineAdmissionService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Student / Parent submits a student leave request | `createLeave` | `STUDENT_LEAVE_REQUESTED` | `ATTENDANCE` | `HIGH` (`TEACHER`), `NORMAL` (`ADMIN`) | `TEACHER` (class teachers), `ADMIN` | `/students/leaves` |
| Teacher / Admin approves or rejects student leave | `updateLeave` | `STUDENT_LEAVE_STATUS_UPDATED` | `ATTENDANCE` | `HIGH` | `STUDENT`, `PARENT` | `/students/leaves` |
| New online admission application submitted | `createAdmission` | `ONLINE_ADMISSION_SUBMITTED` | `FRONTOFFICE` | `HIGH` | `ADMIN` | `/students/online-admissions` |
| Online admission application status updated | `updateStatus` | `ONLINE_ADMISSION_STATUS_UPDATED` | `FRONTOFFICE` | `NORMAL` | `ADMIN` | `/students/online-admissions` |

---

### 2.7 Front Office (`VisitorLogService`, `GatePassService`, `ParcelReceiveService`, `SchoolComplaintService`, `AdmissionEnquiryService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Visitor checks in at the front desk | `createVisitor` | `VISITOR_CHECKED_IN` | `FRONTOFFICE` | `HIGH` (host staff), `LOW` (`ADMIN`) | `TEACHER`/`STAFF` (`meetingWithStaff`), `ADMIN` | `/front-office/visitors` |
| Gate pass issued for a student or staff member | `createGatePass` | `GATE_PASS_ISSUED` | `FRONTOFFICE` | `HIGH` (`STUDENT`/`PARENT`), `NORMAL` (`STAFF`), `LOW` (`ADMIN`) | `STUDENT`, `PARENT`, `TEACHER`/`STAFF`, `ADMIN` | `/front-office/gate-passes` |
| Parcel received at the front desk | `receiveParcel` | `PARCEL_RECEIVED` | `FRONTOFFICE` | `NORMAL` (staff), `LOW` (`ADMIN`) | `TEACHER`/`STAFF` (`receivedByStaff`), `ADMIN` | `/front-office/parcels` |
| Parcel marked as collected | `markCollected` | `PARCEL_COLLECTED` | `FRONTOFFICE` | `LOW` | `ADMIN` | `/front-office/parcels` |
| New school complaint filed | `createComplaint` | `COMPLAINT_FILED` | `FRONTOFFICE` | `HIGH` | `ADMIN` | `/front-office/complaints` |
| Complaint status updated to `Resolved` or `Closed` | `updateComplaint`, `updateComplaintStatus` | `COMPLAINT_RESOLVED` | `FRONTOFFICE` | `NORMAL` | `ADMIN` | `/front-office/complaints` |
| New admission enquiry logged | `createEnquiry` | `ADMISSION_ENQUIRY_CREATED` | `FRONTOFFICE` | `NORMAL` | `TEACHER`/`STAFF` (`assignedTo`), `ADMIN` | `/front-office/enquiries` |

---

### 2.8 Library Circulation (`SchoolLibraryService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Book issued to a library member (Student or Staff) | `issueBook` | `BOOK_ISSUED` | `LIBRARY` | `LOW` | `STUDENT` & `PARENT` (if student)<br>`TEACHER` or `STAFF` (if staff) | `/library/circulation` |
| Book returned by a library member (includes fine details if overdue) | `returnBook` | `BOOK_RETURNED` | `LIBRARY` | `NORMAL` (if fine > 0) or `LOW` | `STUDENT` & `PARENT` (if student)<br>`TEACHER` or `STAFF` (if staff) | `/library/circulation` |

---

### 2.9 Communication Center & Support Tickets (`CommunicationCenterService`, `SupportTicketService`)

| Event / Action | Method | `NotificationType` | Category | Priority | Target Roles Notified | Deep Link (`actionUrl`) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Super Admin sends an announcement (`CommunicationStatus.SENT`) | `createAnnouncement` | `CIRCULAR_ANNOUNCEMENT` / `EMERGENCY_ALERT` | `COMMUNICATION` | `URGENT` or `NORMAL` | `ADMIN` (across all targeted schools) | `/notifications` |
| User creates a support ticket | `createTicket` | `SUPPORT_TICKET_CREATED` | `SYSTEM` | `HIGH`/`NORMAL` (`SUPER_ADMIN`), `LOW` (creator) | `SUPER_ADMIN`, Ticket Creator | `/support/tickets` |
| Support ticket assigned to an employee | `assignTicket` | `SUPPORT_TICKET_UPDATED` | `SYSTEM` | `HIGH` (assignee), `NORMAL` (creator) | Assigned Employee (`SUPER_ADMIN`), Ticket Creator | `/support/tickets` |
| Support ticket status updated (`WORKING`, `SOLVED`, `CLOSED`, etc.) | `updateStatus` | `SUPPORT_TICKET_UPDATED` | `SYSTEM` | `NORMAL` | Ticket Creator | `/support/tickets` |

---

## 3. Role-by-Role Notification Summary

| Role (`targetRole`) | Notifications Received |
| :--- | :--- |
| **`STUDENT`** | `HOMEWORK_ASSIGNED`, `HOMEWORK_GRADED`, `ATTENDANCE_ABSENT`, `ATTENDANCE_LATE`, `EXAM_SCHEDULE_PUBLISHED`, `EXAM_ADMIT_CARD_READY`, `EXAM_MARKS_ENTERED`, `EXAM_RESULT_PUBLISHED`, `TEACHER_REMARK_ADDED`, `FEE_INVOICE_GENERATED`, `FEE_DISCOUNT_APPLIED`, `FEE_PAYMENT_SUCCESS`, `FEE_DUE_REMINDER`, `STUDENT_LEAVE_STATUS_UPDATED`, `GATE_PASS_ISSUED`, `BOOK_ISSUED`, `BOOK_RETURNED`, `CIRCULAR_ANNOUNCEMENT`, `EVENT_REMINDER`, `EMERGENCY_ALERT` |
| **`PARENT`** | `HOMEWORK_ASSIGNED`, `HOMEWORK_GRADED`, `ATTENDANCE_ABSENT`, `ATTENDANCE_LATE`, `EXAM_SCHEDULE_PUBLISHED`, `EXAM_ADMIT_CARD_READY`, `EXAM_MARKS_ENTERED`, `EXAM_RESULT_PUBLISHED`, `TEACHER_REMARK_ADDED`, `FEE_INVOICE_GENERATED`, `FEE_DISCOUNT_APPLIED`, `FEE_PAYMENT_SUCCESS`, `FEE_DUE_REMINDER`, `STUDENT_LEAVE_STATUS_UPDATED`, `GATE_PASS_ISSUED`, `BOOK_ISSUED`, `BOOK_RETURNED`, `PTM_SCHEDULED`, `CIRCULAR_ANNOUNCEMENT`, `EMERGENCY_ALERT` |
| **`TEACHER`** | `HOMEWORK_SUBMITTED`, `STUDENT_LEAVE_REQUESTED`, `EXAM_SCHEDULE_PUBLISHED`, `EXAM_RESULT_PUBLISHED`, `LEAVE_STATUS_UPDATED`, `PAYROLL_GENERATED`, `PAYROLL_PAID`, `STAFF_TASK_ASSIGNED`, `VISITOR_CHECKED_IN`, `GATE_PASS_ISSUED`, `PARCEL_RECEIVED`, `ADMISSION_ENQUIRY_CREATED`, `BOOK_ISSUED`, `BOOK_RETURNED`, `CIRCULAR_ANNOUNCEMENT`, `EMERGENCY_ALERT`, `SUPPORT_TICKET_CREATED`, `SUPPORT_TICKET_UPDATED` |
| **`STAFF`** | `LEAVE_STATUS_UPDATED`, `PAYROLL_GENERATED`, `PAYROLL_PAID`, `STAFF_TASK_ASSIGNED`, `VISITOR_CHECKED_IN`, `GATE_PASS_ISSUED`, `PARCEL_RECEIVED`, `ADMISSION_ENQUIRY_CREATED`, `BOOK_ISSUED`, `BOOK_RETURNED`, `CIRCULAR_ANNOUNCEMENT`, `EMERGENCY_ALERT`, `SUPPORT_TICKET_CREATED`, `SUPPORT_TICKET_UPDATED` |
| **`ADMIN`** | `LEAVE_REQUESTED`, `STAFF_TASK_UPDATED`, `STUDENT_LEAVE_REQUESTED`, `ONLINE_ADMISSION_SUBMITTED`, `ONLINE_ADMISSION_STATUS_UPDATED`, `FEE_PAYMENT_SUCCESS`, `EXAM_MARKS_ENTERED` (bulk summary), `VISITOR_CHECKED_IN`, `GATE_PASS_ISSUED`, `PARCEL_RECEIVED`, `PARCEL_COLLECTED`, `COMPLAINT_FILED`, `COMPLAINT_RESOLVED`, `ADMISSION_ENQUIRY_CREATED`, `CIRCULAR_ANNOUNCEMENT`, `EMERGENCY_ALERT`, `SUPPORT_TICKET_CREATED`, `SUPPORT_TICKET_UPDATED` |
| **`SUPER_ADMIN`** | `SUPPORT_TICKET_CREATED`, `SUPPORT_TICKET_UPDATED`, `SYSTEM_ALERT` |

