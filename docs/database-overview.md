# Database Overview

This project uses PostgreSQL as the application database. Database access is handled through Spring Data JPA and Hibernate, while schema creation and updates are managed by Flyway migrations.

## Runtime Configuration

The main database configuration is in:

`src/main/resources/application.properties`

Current configured database:

| Property | Value |
| --- | --- |
| Database engine | PostgreSQL |
| JDBC URL | `jdbc:postgresql://dpg-d7fo3mmrnols73f6i3jg-a.oregon-postgres.render.com:5432/school_erp_db_8r8k` |
| Database name | `school_erp_db_8r8k` |
| Username | `school_erp_db_8r8k_user` |
| Hibernate DDL mode | `validate` |
| Flyway enabled | `true` |
| Flyway location | `classpath:db/migration` |

The password is currently present in `application.properties`. It should be moved to an environment variable or secrets manager before production use.

## Dependencies

Database-related dependencies are declared in `pom.xml`:

- `spring-boot-starter-data-jpa`
- `org.postgresql:postgresql`
- `spring-boot-starter-flyway`
- `flyway-core`
- `flyway-database-postgresql`

## JPA and Hibernate Settings

Important settings:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
```

`ddl-auto=validate` means Hibernate checks that the database schema matches the entity mappings, but it does not create or update tables automatically. Flyway is responsible for schema changes.

## Connection Pool

The project uses Spring Boot's default HikariCP connection pool:

```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=1
spring.datasource.hikari.idle-timeout=30000
spring.datasource.hikari.max-lifetime=240000
spring.datasource.hikari.keepalive-time=30000
spring.datasource.hikari.connection-timeout=30000
```

## Flyway Migrations

Migration files are stored in:

`src/main/resources/db/migration`

Current migrations:

| File | Purpose |
| --- | --- |
| `V1__init.sql` | Creates the initial school ERP tables. |
| `V2__multi_tenant_auth.sql` | Renames `school` to `schools`, adds auth and multi-tenant tables, and creates the `user_role` enum. |
| `V3__sample_data.sql` | Inserts sample schools, users, roles, classes, staff, students, attendance, and invoices. |

## Tables

Current tables:

- `schools`
- `class`
- `student`
- `staff`
- `fee_invoice`
- `payment`
- `student_attendance`
- `users`
- `user_school_roles`
- `student_parents`
- `auth_sessions`

PostgreSQL enum:

```sql
user_role = SUPER_ADMIN, ADMIN, TEACHER, STUDENT, PARENT
```

## Entity Mapping

| Entity | Table |
| --- | --- |
| `School` | `schools` |
| `SchoolClass` | `class` |
| `Student` | `student` |
| `Staff` | `staff` |
| `FeeInvoice` | `fee_invoice` |
| `Payment` | `payment` |
| `Attendance` | `student_attendance` |
| `User` | `users` |
| `UserSchoolRole` | `user_school_roles` |
| `StudentParent` | `student_parents` |
| `AuthSession` | `auth_sessions` |

## Main Relationships

| Relationship | Foreign key |
| --- | --- |
| Class belongs to school | `class.school_id -> schools.id` |
| Student belongs to class | `student.class_id -> class.id` |
| Student belongs to school | `student.school_id -> schools.id` |
| Staff belongs to school | `staff.school_id -> schools.id` |
| Fee invoice belongs to student | `fee_invoice.student_id -> student.id` |
| Fee invoice belongs to school | `fee_invoice.school_id -> schools.id` |
| Payment belongs to invoice | `payment.invoice_id -> fee_invoice.id` |
| Payment belongs to school | `payment.school_id -> schools.id` |
| Attendance belongs to student | `student_attendance.student_id -> student.id` |
| Attendance belongs to school | `student_attendance.school_id -> schools.id` |
| User role belongs to user | `user_school_roles.user_id -> users.id` |
| User role belongs to school | `user_school_roles.school_id -> schools.id` |
| Student parent belongs to student | `student_parents.student_id -> student.id` |
| Student parent belongs to parent user | `student_parents.parent_user_id -> users.id` |
| Auth session belongs to user | `auth_sessions.user_id -> users.id` |
| Auth session optionally belongs to school | `auth_sessions.school_id -> schools.id` |

## Deployment Notes

AWS deployment files also include database configuration:

- `deploy/aws/create-rds.ps1`
- `deploy/aws/deploy-backend-ecs.ps1`
- `deploy/aws/ecs-taskdef.template.json`

The RDS script creates a PostgreSQL database with:

| Setting | Value |
| --- | --- |
| DB identifier | `school-erp-postgres` |
| DB name | `school_erp` |
| DB username | `school_erp_app` |
| Instance class | `db.t3.micro` |
| Storage | `30 GB` |
| Backups | `1 day` |
| Storage encryption | Enabled |
| Multi-AZ | Disabled |
| Public access | Enabled |

The ECS task definition expects these environment variables and secrets:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=validate`

## Security Notes

- Rotate the current database password if it has been shared or committed.
- Do not store database passwords in source control.
- Use environment variables locally.
- Use AWS Secrets Manager or another secrets manager in deployed environments.
- Restrict database network access to only trusted IP addresses or application security groups.
