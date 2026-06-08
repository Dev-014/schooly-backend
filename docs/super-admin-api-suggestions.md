# Super Admin API Design Suggestions

This document outlines the API endpoints required for the `SUPER_ADMIN` role to manage the School ERP SaaS platform, as defined in the [Access Control & Module System](./school-erp-access-control-modules.md) document.

All Super Admin APIs should be prefixed with `/super-admin` to clearly separate platform-level actions from school-level actions.

---

## 1. School Management
Manage the lifecycle of school tenants on the platform.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/schools` | `GET` | List all schools with pagination and search. |
| `/super-admin/schools` | `POST` | Onboard a new school and its primary admin. |
| `/super-admin/schools/{id}` | `GET` | View detailed school profile and stats. |
| `/super-admin/schools/{id}` | `PUT` | Update school details. |
| `/super-admin/schools/{id}/status` | `PATCH` | Update school status (`ACTIVE`, `SUSPENDED`). |

### Payloads: School Management

**POST `/super-admin/schools` (Onboard School)**
- **Request:**
```json
{
  "name": "Greenwood International",
  "address": "123 Education Lane, City",
  "contactEmail": "info@greenwood.com",
  "contactPhone": "+1234567890",
  "adminName": "John Doe",
  "adminEmail": "admin@greenwood.com",
  "adminPassword": "TemporaryPassword123",
  "planId": 1
}
```
- **Response (201 Created):**
```json
{
  "id": 50,
  "name": "Greenwood International",
  "status": "ACTIVE",
  "slug": "greenwood-international",
  "adminUser": {
    "id": 101,
    "email": "admin@greenwood.com",
    "name": "John Doe"
  }
}
```

**PATCH `/super-admin/schools/{id}/status`**
- **Request:**
```json
{
  "status": "SUSPENDED",
  "reason": "Payment overdue for 2 months"
}
```
- **Response (200 OK):**
```json
{
  "id": 50,
  "status": "SUSPENDED",
  "updatedAt": "2023-10-27T10:00:00Z"
}
```

---

## 2. Module & Permission Management
Control which features are available globally and per school.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/modules` | `GET` | List all globally defined modules. |
| `/super-admin/modules` | `POST` | Define a new module in the system. |
| `/super-admin/schools/{id}/modules/toggle` | `POST` | Enable/disable a specific module for a school. |

### Payloads: Module Management

**POST `/super-admin/modules` (Define Module)**
- **Request:**
```json
{
  "code": "library_management",
  "name": "Library Management",
  "description": "Books, issues, returns and fine management",
  "isDefault": false
}
```
- **Response (201 Created):**
```json
{ "id": 10, "code": "library_management", "status": "ACTIVE" }
```

**POST `/super-admin/schools/{id}/modules/toggle`**
- **Request:**
```json
{
  "moduleCode": "transport",
  "enabled": true
}
```
- **Response (200 OK):**
```json
{
  "schoolId": 50,
  "moduleCode": "transport",
  "enabled": true,
  "updatedAt": "2023-10-27T10:05:00Z"
}
```

---

## 3. Subscription & Billing (Future)
Manage plans and school subscriptions.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/plans` | `POST` | Create a new subscription plan. |
| `/super-admin/schools/{id}/subscription` | `PUT` | Manually update school's plan. |

### Payloads: Subscription

**POST `/super-admin/plans`**
- **Request:**
```json
{
  "name": "Premium Plan",
  "monthlyPrice": 199.99,
  "moduleCodes": ["student_management", "fees", "exams", "transport", "library"],
  "limits": {
    "maxStudents": 5000,
    "storageGb": 50
  }
}
```

---

## 4. Platform User Management
Manage internal staff who operate the SaaS platform.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/users` | `POST` | Create a new platform operator. |

### Payloads: Platform Users

**POST `/super-admin/users`**
- **Request:**
```json
{
  "name": "Support Agent Alpha",
  "email": "agent.a@platform.com",
  "role": "SUPER_ADMIN", 
  "permissions": ["school.read", "audit.read", "support.impersonate"]
}
```

---

## 5. Audit & Monitoring
Monitor system activity and sensitive changes.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/audit-logs` | `GET` | Global audit log with filters. |

### Payloads: Audit

**GET `/super-admin/audit-logs?schoolId=50&action=IMPERSONATION_START`**
- **Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1001,
      "actorId": 1,
      "actorName": "System Admin",
      "action": "SUPPORT_IMPERSONATION_START",
      "targetSchoolId": 50,
      "targetSchoolName": "Greenwood International",
      "timestamp": "2023-10-27T09:00:00Z",
      "ipAddress": "192.168.1.1"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 6. Support & School Context (Impersonation)

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/support/impersonate/{schoolId}` | `POST` | Generates a temporary school-scoped JWT. |

### Payloads: Support

**POST `/super-admin/support/impersonate/50`**
- **Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": "2023-10-27T09:30:00Z",
  "schoolName": "Greenwood International",
  "permissions": ["student.read", "fee.read"]
}
```

---

## 7. Global Configuration

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/settings/global` | `PUT` | Update global settings. |

### Payloads: Global Config

**PUT `/super-admin/settings/global`**
- **Request:**
```json
{
  "maintenanceMode": false,
  "minAppVersion": "2.1.0",
  "supportEmail": "support@saas-platform.com",
  "allowedDomains": ["*.schooly-erp.com"]
}
```

---

## 8. Isolated School Overrides
Direct actions on a school without full impersonation.

| Endpoint | Method | Description |
| --- | --- | --- |
| `/super-admin/schools/{id}/config-override` | `PATCH` | Override specific school limits. |
| `/super-admin/schools/{id}/reset-admin-password` | `POST` | Trigger reset for school admin. |

### Payloads: Overrides

**PATCH `/super-admin/schools/50/config-override`**
- **Request:**
```json
{
  "studentLimit": 600,
  "enableBetaFeatures": true
}
```

**POST `/super-admin/schools/50/reset-admin-password`**
- **Response (200 OK):**
```json
{
  "message": "Password reset link sent to admin@greenwood.com",
  "tempPassword": "AutoGenerated123!" 
}
```

---

## Example Usage Scenarios

### Scenario A: Enabling a module for a school
1.  `GET /super-admin/schools?search=Greenwood` -> returns `id: 50`.
2.  `POST /super-admin/schools/50/modules/toggle` with body `{ "moduleCode": "transport", "enabled": true }`.
3.  Audit log is written.

### Scenario B: Investigating a student record for a school
1.  `POST /super-admin/support/impersonate/50` -> returns a new JWT.
2.  Use the new JWT to call `GET /api/students` (standard school API).
3.  All actions performed with this token are logged with `user_id` of the Super Admin but `school_id` 50.
