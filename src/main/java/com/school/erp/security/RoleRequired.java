package com.school.erp.security;

import com.school.erp.entity.UserRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce role-based access control on controller methods.
 * The authenticated user must have one of the specified roles to access the endpoint.
 *
 * Example: @RoleRequired({UserRole.SUPER_ADMIN, UserRole.ADMIN})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleRequired {
    UserRole[] value();
}
