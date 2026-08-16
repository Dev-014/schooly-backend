package com.school.erp.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce dynamic permission-based access control.
 * Replaces @RoleRequired.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionRequired {
    String value(); // The permissionKey e.g. "attendance.attendance_record.edit"
}
