# Super Admin API - Implementation Plan

**Document Date:** 2026-06-08  
**Based on:** super-admin-api-suggestions.md  
**Status:** In Progress

---

## Table of Contents
1. [Overview & Architecture](#overview--architecture)
2. [Database Schema Changes](#database-schema-changes)
3. [Entities, DTOs & Models](#entities-dtos--models)
4. [Implementation Phases](#implementation-phases)
5. [Detailed Endpoint Breakdown](#detailed-endpoint-breakdown)
6. [Security & Authorization](#security--authorization)
7. [Testing Strategy](#testing-strategy)

---

## Overview & Architecture

### API Prefix
All endpoints are prefixed with `/super-admin` to separate platform-level actions from school-level actions.

### Key Principles
- **Soft Multitenancy**: Super Admins can impersonate schools via temporary JWT tokens
- **Audit Logging**: All super admin actions must be tracked
- **Role-Based Access**: Enforce SUPER_ADMIN role at endpoint level
- **Backwards Compatibility**: Existing school-scoped APIs remain unchanged

### Technology Stack
- **Language:** Java (Spring Boot)
- **Framework:** Spring Security, Spring Data JPA
- **Database:** PostgreSQL
- **Audit:** Custom AuditLog entity

---

## Database Schema Changes

### 1. New Tables Required

#### `platform_modules`
```sql
CREATE TABLE platform_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `school_module_access`
```sql
CREATE TABLE school_module_access (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    enabled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES platform_modules(id) ON DELETE CASCADE,
    UNIQUE KEY unique_school_module (school_id, module_id)
);
```

#### `subscription_plans`
```sql
CREATE TABLE subscription_plans (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    monthly_price DECIMAL(10, 2),
    max_students INT,
    storage_gb INT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `plan_modules`
```sql
CREATE TABLE plan_modules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE,
    FOREIGN KEY (module_id) REFERENCES platform_modules(id) ON DELETE CASCADE,
    UNIQUE KEY unique_plan_module (plan_id, module_id)
);
```

#### `platform_users`
```sql
CREATE TABLE platform_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(500),
    role VARCHAR(50) DEFAULT 'SUPER_ADMIN',
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `audit_logs` (Enhanced)
```sql
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor_id BIGINT,
    actor_name VARCHAR(255),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id BIGINT,
    target_school_id BIGINT,
    target_school_name VARCHAR(255),
    changes_json JSON,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    status VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_school_id) REFERENCES schools(id)
);
```

#### `school_config_overrides`
```sql
CREATE TABLE school_config_overrides (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    school_id BIGINT NOT NULL,
    student_limit INT,
    enable_beta_features BOOLEAN DEFAULT FALSE,
    custom_config JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
    UNIQUE KEY unique_school_config (school_id)
);
```

### 2. Existing Tables Modifications

#### `schools` table - Add columns
```sql
ALTER TABLE schools ADD COLUMN subscription_plan_id BIGINT AFTER id;
ALTER TABLE schools ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE' AFTER subscription_plan_id;
ALTER TABLE schools ADD FOREIGN KEY (subscription_plan_id) REFERENCES subscription_plans(id);
ALTER TABLE schools ADD COLUMN slug VARCHAR(255) UNIQUE AFTER name;
ALTER TABLE schools ADD COLUMN created_by BIGINT;
```

#### `auth_users` table - Add columns
```sql
ALTER TABLE auth_users ADD COLUMN created_by BIGINT;
ALTER TABLE auth_users ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
```

---

## Entities, DTOs & Models

### Entities to Create

#### 1. `PlatformModule.java`
```java
@Entity
@Table(name = "platform_modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformModule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String code;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Enumerated(EnumType.STRING)
    private ModuleStatus status = ModuleStatus.ACTIVE;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### 2. `SchoolModuleAccess.java`
```java
@Entity
@Table(name = "school_module_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolModuleAccess {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
    
    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private PlatformModule module;
    
    private Boolean enabled = true;
    
    @CreationTimestamp
    private LocalDateTime enabledAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### 3. `SubscriptionPlan.java`
```java
@Entity
@Table(name = "subscription_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private BigDecimal monthlyPrice;
    private Integer maxStudents;
    private Integer storageGb;
    
    @Enumerated(EnumType.STRING)
    private PlanStatus status = PlanStatus.ACTIVE;
    
    @ManyToMany
    @JoinTable(
        name = "plan_modules",
        joinColumns = @JoinColumn(name = "plan_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private Set<PlatformModule> modules = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

#### 4. `AuditLog.java` (Enhanced)
```java
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long actorId;
    private String actorName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    private String resourceType;
    private Long resourceId;
    private Long targetSchoolId;
    private String targetSchoolName;
    
    @Column(columnDefinition = "JSON")
    private String changesJson;
    
    private String ipAddress;
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    private AuditStatus status = AuditStatus.SUCCESS;
    
    @CreationTimestamp
    private LocalDateTime timestamp;
}
```

#### 5. `SchoolConfigOverride.java`
```java
@Entity
@Table(name = "school_config_overrides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolConfigOverride {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
    
    private Integer studentLimit;
    private Boolean enableBetaFeatures = false;
    
    @Column(columnDefinition = "JSON")
    private String customConfig;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

### DTOs to Create

#### Request DTOs
1. `OnboardSchoolRequest.java` - School onboarding payload
2. `CreatePlatformModuleRequest.java` - Module creation
3. `ToggleModuleRequest.java` - Module toggle
4. `UpdateSchoolStatusRequest.java` - Status update
5. `CreateSubscriptionPlanRequest.java` - Plan creation
6. `CreatePlatformUserRequest.java` - Platform user creation
7. `GlobalSettingsRequest.java` - Global config
8. `SchoolConfigOverrideRequest.java` - Config override

#### Response DTOs
1. `SchoolResponseDTO.java` - School details
2. `PlatformModuleDTO.java` - Module details
3. `SubscriptionPlanDTO.java` - Plan details
4. `AuditLogDTO.java` - Audit log details
5. `ImpersonationTokenResponse.java` - Impersonation response
6. `PaginatedResponse<T>.java` - Generic paginated response

---

## Implementation Phases

### Phase 1: Foundation & Core Infrastructure (Week 1)
**Priority:** HIGH | **Effort:** Medium

- [ ] Create all database tables and run Flyway migrations
- [ ] Create entity classes (PlatformModule, SchoolModuleAccess, AuditLog, SchoolConfigOverride)
- [ ] Create repositories (JpaRepository extensions)
- [ ] Create request/response DTOs
- [ ] Create enums (ModuleStatus, AuditAction, AuditStatus, etc.)
- [ ] Setup audit logging service

**Files to Create:**
- `entity/PlatformModule.java`
- `entity/SchoolModuleAccess.java`
- `entity/SubscriptionPlan.java`
- `entity/AuditLog.java`
- `entity/SchoolConfigOverride.java`
- `repository/PlatformModuleRepository.java`
- `repository/SchoolModuleAccessRepository.java`
- `repository/AuditLogRepository.java`
- `service/AuditLogService.java`

---

### Phase 2: School Management (Week 1-2)
**Priority:** HIGH | **Effort:** Medium

**Endpoints:**
- `GET /super-admin/schools` - List with pagination/search
- `POST /super-admin/schools` - Onboard new school
- `GET /super-admin/schools/{id}` - View school details
- `PUT /super-admin/schools/{id}` - Update school
- `PATCH /super-admin/schools/{id}/status` - Toggle status

**Files to Create:**
- `controller/SuperAdminSchoolController.java`
- `service/SuperAdminSchoolService.java`
- `dto/request/OnboardSchoolRequest.java`
- `dto/request/UpdateSchoolStatusRequest.java`
- `dto/response/SchoolResponseDTO.java`

**Key Logic:**
- Validate school onboarding data
- Generate unique slug for school
- Create initial admin user with default password
- Log all school creation/updates in audit logs
- Trigger welcome email to admin (future)

---

### Phase 3: Module & Permission Management (Week 2)
**Priority:** HIGH | **Effort:** Medium

**Endpoints:**
- `GET /super-admin/modules` - List all modules
- `POST /super-admin/modules` - Define new module
- `POST /super-admin/schools/{id}/modules/toggle` - Enable/disable module

**Files to Create:**
- `controller/SuperAdminModuleController.java`
- `service/SuperAdminModuleService.java`
- `dto/request/CreatePlatformModuleRequest.java`
- `dto/request/ToggleModuleRequest.java`
- `dto/response/PlatformModuleDTO.java`

**Key Logic:**
- Prevent duplicate module codes
- Default modules should be auto-enabled for new schools
- Log module access changes
- Validate module exists before toggling

---

### Phase 4: Subscription & Billing (Week 3)
**Priority:** MEDIUM | **Effort:** Medium

**Endpoints:**
- `POST /super-admin/plans` - Create subscription plan
- `PUT /super-admin/schools/{id}/subscription` - Update school plan

**Files to Create:**
- `controller/SuperAdminBillingController.java`
- `service/SuperAdminBillingService.java`
- `dto/request/CreateSubscriptionPlanRequest.java`
- `repository/SubscriptionPlanRepository.java`
- `repository/PlanModuleRepository.java`
- `entity/SubscriptionPlan.java`

**Key Logic:**
- Associate modules with plans
- Validate plan limits
- Handle plan transitions
- Update school module access based on plan

---

### Phase 5: Support & Impersonation (Week 3)
**Priority:** HIGH | **Effort:** High

**Endpoints:**
- `POST /super-admin/support/impersonate/{schoolId}` - Generate temporary JWT

**Files to Create:**
- `controller/SuperAdminSupportController.java`
- `service/SuperAdminImpersonationService.java`
- `service/JwtTokenProvider.java` (enhance existing)
- `dto/response/ImpersonationTokenResponse.java`

**Key Logic:**
- Generate school-scoped JWT with limited permissions
- Token expiry: 30 minutes (configurable)
- All actions with impersonation token are logged
- Include school context in JWT claims
- Validate school exists and is active

---

### Phase 6: Audit & Monitoring (Week 4)
**Priority:** MEDIUM | **Effort:** Medium

**Endpoints:**
- `GET /super-admin/audit-logs` - Global audit logs with filters

**Files to Create:**
- `controller/SuperAdminAuditController.java`
- `service/SuperAdminAuditService.java`
- `dto/response/AuditLogDTO.java`

**Key Logic:**
- Support filtering by: schoolId, action, actor, dateRange
- Pagination support
- Include all super admin actions
- Support exporting logs (future)

---

### Phase 7: Global Configuration & Overrides (Week 4)
**Priority:** LOW | **Effort:** Medium

**Endpoints:**
- `PUT /super-admin/settings/global` - Update global settings
- `PATCH /super-admin/schools/{id}/config-override` - Override school limits
- `POST /super-admin/schools/{id}/reset-admin-password` - Reset password

**Files to Create:**
- `controller/SuperAdminConfigController.java`
- `service/SuperAdminConfigService.java`
- `entity/GlobalSettings.java`
- `entity/SchoolConfigOverride.java` (if not created in Phase 1)

**Key Logic:**
- Store global settings in config entity
- Allow school-level overrides
- Generate temporary password for reset
- Send reset link via email

---

## Detailed Endpoint Breakdown

### School Management Endpoints

#### 1. GET /super-admin/schools
```
Query Parameters: page=0, size=10, search="greenwood", status="ACTIVE"
Response: 
{
  "content": [ ... ],
  "totalElements": 50,
  "totalPages": 5,
  "currentPage": 0
}
```

#### 2. POST /super-admin/schools
```
Body: OnboardSchoolRequest
  - name
  - address
  - contactEmail
  - contactPhone
  - adminName
  - adminEmail
  - adminPassword
  - planId

Response (201): SchoolResponseDTO with adminUser details
```

#### 3. GET /super-admin/schools/{id}
```
Response: SchoolResponseDTO with modules, plan, stats
```

#### 4. PUT /super-admin/schools/{id}
```
Body: Fields to update (name, address, contactEmail, contactPhone)
Response: Updated SchoolResponseDTO
```

#### 5. PATCH /super-admin/schools/{id}/status
```
Body: UpdateSchoolStatusRequest
  - status: "ACTIVE" | "SUSPENDED"
  - reason: string (optional)

Response: { id, status, updatedAt }
```

---

## Security & Authorization

### 1. Authentication & Authorization
- All super admin endpoints require `SUPER_ADMIN` role
- Implement `@PreAuthorize("hasRole('SUPER_ADMIN')")` on controller methods
- Validate in SecurityConfig that super admin users have explicit permission

### 2. Request Validation
- Add validation annotations on all DTOs (@NotNull, @NotBlank, etc.)
- Validate business logic (e.g., school exists before toggling modules)
- Sanitize input to prevent injection attacks

### 3. Response Security
- Never expose sensitive data (password hashes, API keys)
- Use DTOs to carefully select what to return
- Implement field-level security if needed

### 4. Audit Trail
- Log all super admin actions with actor details
- Include IP address and user agent
- Track changes (before/after JSON)
- Mark failures with reason

### 5. Impersonation Token Security
- Use strong JWT secret key
- Include school context as claim (non-editable)
- Set short expiry (30 minutes)
- Log every impersonation action
- Include token in audit log if accessed with super admin credentials

---

## Testing Strategy

### Unit Tests
- Service layer: Mock repositories, test business logic
- DTO validation: Ensure all DTOs validate correctly
- Enum usage: Test status transitions

### Integration Tests
- Controller endpoints: Test request/response with embedded DB
- Database migrations: Verify schema changes work correctly
- Audit logging: Ensure all actions are captured

### Security Tests
- Role-based access: Verify non-super-admin cannot access endpoints
- Input validation: Test with malicious/invalid inputs
- JWT impersonation: Verify token is school-scoped

### Sample Test Cases

**Phase 2 (School Management)**
```
✓ Onboard school successfully
✓ Onboard school - duplicate email
✓ List schools with pagination
✓ Update school details
✓ Suspend school and verify
✓ Cannot onboard school with invalid plan
```

**Phase 3 (Modules)**
```
✓ List all platform modules
✓ Create module - success
✓ Create module - duplicate code
✓ Enable module for school
✓ Disable module for school
✓ Cannot toggle non-existent module
```

**Phase 5 (Impersonation)**
```
✓ Generate impersonation token
✓ Token includes school context
✓ Token expires after 30 minutes
✓ Action with token is logged
✓ Cannot impersonate suspended school
✓ Cannot impersonate non-existent school
```

---

## API Endpoint Summary

| Phase | Category | Endpoint | Method | Status |
|-------|----------|----------|--------|--------|
| 2 | School Mgmt | `/super-admin/schools` | GET | TODO |
| 2 | School Mgmt | `/super-admin/schools` | POST | TODO |
| 2 | School Mgmt | `/super-admin/schools/{id}` | GET | TODO |
| 2 | School Mgmt | `/super-admin/schools/{id}` | PUT | TODO |
| 2 | School Mgmt | `/super-admin/schools/{id}/status` | PATCH | TODO |
| 3 | Module Mgmt | `/super-admin/modules` | GET | TODO |
| 3 | Module Mgmt | `/super-admin/modules` | POST | TODO |
| 3 | Module Mgmt | `/super-admin/schools/{id}/modules/toggle` | POST | TODO |
| 4 | Billing | `/super-admin/plans` | POST | TODO |
| 4 | Billing | `/super-admin/schools/{id}/subscription` | PUT | TODO |
| 5 | Support | `/super-admin/support/impersonate/{schoolId}` | POST | TODO |
| 6 | Audit | `/super-admin/audit-logs` | GET | TODO |
| 7 | Config | `/super-admin/settings/global` | PUT | TODO |
| 7 | Config | `/super-admin/schools/{id}/config-override` | PATCH | TODO |
| 7 | Config | `/super-admin/schools/{id}/reset-admin-password` | POST | TODO |

---

## Migration Scripts

### Flyway Migration: V4__super_admin_tables.sql
- Create all new tables
- Add foreign keys
- Create indexes for performance
- Add sample data for modules

---

## Next Steps

1. **Review & Approval** - Share this plan with stakeholders
2. **Database Setup** - Create migration script and run tests
3. **Phase 1 Implementation** - Start with entities and repositories
4. **Incremental Testing** - Write tests as code progresses
5. **API Documentation** - Generate Swagger/OpenAPI docs
6. **Load Testing** - Test audit logging performance at scale

---

## Notes & Considerations

- **Email Integration**: Placeholder for onboarding emails (configure SMTP)
- **Bulk Operations**: Consider implementing bulk school operations in Phase 2+
- **Rate Limiting**: Implement rate limiting on super admin endpoints
- **Caching**: Cache platform modules as they rarely change
- **Search Optimization**: Add full-text search for large school directories
- **Webhook Support**: Consider webhooks for school status changes
- **API Versioning**: Use `/super-admin/v1/...` if planning multiple versions
