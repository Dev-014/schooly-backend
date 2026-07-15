# Schooly Backend – Production Level API Documentation

## Document Information

| Field             | Details                            |
| ----------------- | ---------------------------------- |
| Project Name      | Schooly Backend                    |
| Repository        | Dev-014/schooly-backend            |
| Document Type     | Backend API & System Documentation |
| Architecture Type | RESTful Backend Service            |
| Technology Stack  | Java, Spring Boot, Maven           |

---

# Executive Summary

Schooly Backend is a centralized school ERP backend system developed using Spring Boot architecture. The system provides scalable REST APIs for managing authentication, schools, students, staff, attendance, fee invoices, payments, dashboards, and parent interactions.

The backend follows a modular controller-service architecture and exposes REST endpoints for frontend and third-party integrations. The system is designed to support multi-school management and operational workflows required by educational institutions.

Key objectives of the backend:

* Centralized school management
* Student lifecycle management
* Staff and administrative management
* Attendance monitoring
* Fee collection and invoice tracking
* Dashboard analytics and KPIs
* Parent interaction support
* Secure authentication and authorization

---

# System Architecture

## Project Overview

Backend service for the Schooly ERP system built using Spring Boot. The APIs manage authentication, students, staff, schools, attendance, invoices, payments, dashboards, and parent-related features.

---

## Backend Architecture Overview

The application follows a layered architecture:

```text
Controller Layer
↓
Service Layer
↓
Repository/Data Access Layer
↓
Database
```

### Core Components

| Layer               | Responsibility                         |
| ------------------- | -------------------------------------- |
| Controller Layer    | Handles HTTP requests and API routing  |
| Service Layer       | Business logic and validations         |
| Repository Layer    | Database operations                    |
| Entity Layer        | Data models/entities                   |
| DTO Layer           | Request/response transformation        |
| Configuration Layer | Security and application configuration |

---

# API Base Structure

Most APIs are prefixed with:

```text
/api
```

Authentication-related APIs use:

```text
/auth
```

Parent-specific APIs use:

```text
/parent
```

---

# Authentication APIs

Controller: `AuthController`

| Method | Endpoint              | Purpose                            |
| ------ | --------------------- | ---------------------------------- |
| POST   | `/auth/login`         | User login/authentication          |
| GET    | `/auth/schools`       | Fetch schools associated with user |
| POST   | `/auth/select-school` | Select active school/session       |

---

# Student APIs

Controller: `StudentController`

| Method | Endpoint             | Purpose                |
| ------ | -------------------- | ---------------------- |
| GET    | `/api/students`      | Get all students       |
| GET    | `/api/students/{id}` | Get student by ID      |
| POST   | `/api/students`      | Create new student     |
| PUT    | `/api/students/{id}` | Update student details |
| DELETE | `/api/students/{id}` | Delete student         |

---

# Staff APIs

Controller: `StaffController`

| Method | Endpoint          | Purpose                |
| ------ | ----------------- | ---------------------- |
| GET    | `/api/staff`      | Get all staff members  |
| GET    | `/api/staff/{id}` | Get staff member by ID |
| POST   | `/api/staff`      | Create staff member    |
| PUT    | `/api/staff/{id}` | Update staff details   |
| DELETE | `/api/staff/{id}` | Delete staff member    |

---

# School APIs

Controller: `SchoolController`

| Method | Endpoint            | Purpose          |
| ------ | ------------------- | ---------------- |
| GET    | `/api/schools`      | Get all schools  |
| GET    | `/api/schools/{id}` | Get school by ID |
| POST   | `/api/schools`      | Create school    |
| PUT    | `/api/schools/{id}` | Update school    |
| DELETE | `/api/schools/{id}` | Delete school    |

---

# School Class APIs

Controller: `SchoolClassController`

| Method | Endpoint            | Purpose         |
| ------ | ------------------- | --------------- |
| GET    | `/api/classes`      | Get all classes |
| GET    | `/api/classes/{id}` | Get class by ID |
| POST   | `/api/classes`      | Create class    |
| PUT    | `/api/classes/{id}` | Update class    |
| DELETE | `/api/classes/{id}` | Delete class    |

---

# Attendance APIs

Controller: `AttendanceController`

| Method | Endpoint          | Purpose                  |
| ------ | ----------------- | ------------------------ |
| GET    | `/api/attendance` | Fetch attendance records |
| POST   | `/api/attendance` | Create attendance entry  |

---

# Fee Invoice APIs

Controller: `FeeInvoiceController`

| Method | Endpoint                 | Purpose              |
| ------ | ------------------------ | -------------------- |
| GET    | `/api/fee-invoices`      | Get all fee invoices |
| GET    | `/api/fee-invoices/{id}` | Get invoice by ID    |
| POST   | `/api/fee-invoices`      | Create fee invoice   |
| PUT    | `/api/fee-invoices/{id}` | Update fee invoice   |
| DELETE | `/api/fee-invoices/{id}` | Delete fee invoice   |

---

# Payment APIs

Controller: `PaymentController`

| Method | Endpoint        | Purpose             |
| ------ | --------------- | ------------------- |
| GET    | `/api/payments` | Get payment records |
| POST   | `/api/payments` | Create payment      |

---

# Parent APIs

Controller: `ParentController`

| Method | Endpoint           | Purpose                         |
| ------ | ------------------ | ------------------------------- |
| GET    | `/parent/children` | Fetch children linked to parent |

---

# Dashboard APIs

Controller: `DashboardController`

| Method | Endpoint                                      | Purpose                           |
| ------ | --------------------------------------------- | --------------------------------- |
| GET    | `/api/dashboard/kpis`                         | Fetch dashboard KPIs              |
| GET    | `/api/dashboard/chart/collection-vs-expenses` | Collection vs expenses chart data |
| GET    | `/api/dashboard/activity-feed`                | Recent activities feed            |

---

# Health Check API

Controller: `HealthController`

| Method | Endpoint  | Purpose                         |
| ------ | --------- | ------------------------------- |
| GET    | `/health` | Application health/status check |

---

# Key Backend Modules

* Authentication & Authorization
* Student Management
* Staff Management
* School Management
* Class Management
* Attendance Tracking
* Fee & Invoice Management
* Payment Management
* Parent Portal Support
* Dashboard & Analytics
* Health Monitoring

---

# Technology Stack

| Technology  | Purpose                    |
| ----------- | -------------------------- |
| Java        | Core programming language  |
| Spring Boot | Backend framework          |
| Maven       | Dependency management      |
| REST APIs   | Communication architecture |
| Docker      | Containerization support   |

---

# Security & Authentication

## Authentication Flow

The backend uses dedicated authentication endpoints for user validation and school selection.

### Authentication Features

* User login authentication
* School-based session selection
* Device metadata capture via `User-Agent`
* Multi-school access handling

---

# API Design Standards

## REST Standards Followed

* Resource-based endpoint naming
* Standard HTTP methods
* JSON request/response handling
* Layered architecture implementation
* Modular controller structure

## HTTP Methods Usage

| Method | Usage            |
| ------ | ---------------- |
| GET    | Fetch resources  |
| POST   | Create resources |
| PUT    | Update resources |
| DELETE | Remove resources |

---

# Backend Modules Breakdown

## 1. Authentication Module

Handles user authentication, authorization flow, and school selection.

### Features

* Login management
* Multi-school access handling
* Session initialization

---

## 2. Student Management Module

Responsible for managing student-related operations.

### Features

* Student creation
* Student profile updates
* Student listing
* Student deletion
* Student data retrieval

---

## 3. Staff Management Module

Handles employee/staff operations.

### Features

* Staff onboarding
* Staff profile management
* Staff retrieval
* Staff updates

---

## 4. School Management Module

Handles school configuration and management.

### Features

* School creation
* School configuration
* School updates
* Multi-school support

---

## 5. Attendance Module

Manages attendance records for operational tracking.

### Features

* Attendance marking
* Attendance retrieval
* Attendance management

---

## 6. Finance & Payment Module

Handles fee invoices and payment tracking.

### Features

* Invoice generation
* Payment tracking
* Financial record management
* Fee operations

---

## 7. Dashboard Module

Provides analytics and reporting APIs.

### Features

* KPI analytics
* Activity feeds
* Financial chart data
* Dashboard metrics

---

## 8. Parent Module

Supports parent-side interactions.

### Features

* Child information access
* Parent-child mapping

---

# Deployment Readiness

## Production Readiness Indicators

* Modular architecture
* RESTful API standards
* Docker support available
* Service separation
* Maintainable project structure
* Scalable backend approach

---

# Recommended Future Improvements

| Area          | Recommendation                   |
| ------------- | -------------------------------- |
| Security      | JWT/OAuth2 implementation        |
| Documentation | Swagger/OpenAPI integration      |
| Monitoring    | Centralized logging & monitoring |
| Testing       | Unit & integration test coverage |
| CI/CD         | Automated deployment pipelines   |
| Performance   | API caching & optimization       |

---

# Conclusion

The Schooly Backend project demonstrates a well-structured Spring Boot REST API architecture suitable for school ERP operations. The backend is modular, scalable, and organized according to enterprise backend development standards.

The system already supports major operational workflows including authentication, student management, staff operations, finance handling, dashboard analytics, and parent integration.

This backend structure provides a strong foundation for production-scale educational management systems and can be extended further with advanced security, monitoring, and deployment capabilities.

---

# Notes

* Most modules support standard CRUD operations.
* APIs are REST-based.
* Authentication flow is handled through dedicated auth endpoints.
* Docker configuration is available for deployment.
* This document reflects the actual existing APIs implemented in the codebase.

---

## Short API Descriptions (brief)

- `GET /health`: Returns service status (e.g., { "status": "UP" }).
- `POST /auth/login`: Authenticate user by phone and return user details.
- `GET /auth/schools?userId=`: List schools linked to the given user.
- `POST /auth/select-school`: Set the active school for a user session; returns auth token.

- `GET /api/dashboard/kpis?schoolId=`: Return KPIs/summary metrics for the school.
- `GET /api/dashboard/chart/collection-vs-expenses?year=&schoolId=`: Return collection vs expense chart points for a year.
- `GET /api/dashboard/activity-feed?size=`: Return recent activity feed entries.

- `GET /api/schools`: List all schools.
- `GET /api/schools/{id}`: Get details for a specific school.
- `POST /api/schools`: Create a new school record.
- `PUT /api/schools/{id}`: Update an existing school's details.
- `DELETE /api/schools/{id}`: Remove a school.

- `GET /api/classes?schoolId=`: List classes, optionally filtered by school.
- `GET /api/classes/{id}`: Get details for a specific class.
- `POST /api/classes`: Create a new class.
- `PUT /api/classes/{id}`: Update class information.
- `DELETE /api/classes/{id}`: Delete a class.

- `GET /api/students?schoolId=&classId=`: List students with optional filters.
- `GET /api/students/{id}`: Retrieve a student's profile.
- `POST /api/students`: Create a new student record.
- `PUT /api/students/{id}`: Update student details.
- `DELETE /api/students/{id}`: Deactivate/delete a student.

- `GET /api/staff?schoolId=`: List staff members, optionally by school.
- `GET /api/staff/{id}`: Get staff member details.
- `POST /api/staff`: Add a new staff member.
- `PUT /api/staff/{id}`: Update staff details.
- `DELETE /api/staff/{id}`: Delete a staff member.

- `GET /api/fee-invoices?schoolId=&studentId=`: List fee invoices with optional filters.
- `GET /api/fee-invoices/{id}`: Get a fee invoice by ID.
- `POST /api/fee-invoices`: Create a new fee invoice.
- `PUT /api/fee-invoices/{id}`: Update an existing invoice.
- `DELETE /api/fee-invoices/{id}`: Delete an invoice.

- `GET /api/payments?schoolId=&invoiceId=`: List payment records.
- `POST /api/payments`: Record a new payment against an invoice.

- `GET /api/attendance?schoolId=&studentId=`: Fetch attendance records.
- `POST /api/attendance`: Create a new attendance entry for a student.

- `GET /parent/children`: Return children linked to the authenticated parent user.

