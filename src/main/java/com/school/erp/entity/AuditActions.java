package com.school.erp.entity;

/**
 * Audit action constants for super admin operations.
 */
public final class AuditActions {
    public static final String SCHOOL_CREATED = "SCHOOL_CREATED";
    public static final String SCHOOL_UPDATED = "SCHOOL_UPDATED";
    public static final String SCHOOL_STATUS_CHANGED = "SCHOOL_STATUS_CHANGED";
    public static final String SCHOOL_SUSPENDED = "SCHOOL_SUSPENDED";
    public static final String SCHOOL_ACTIVATED = "SCHOOL_ACTIVATED";

    public static final String MODULE_CREATED = "MODULE_CREATED";
    public static final String MODULE_ENABLED = "MODULE_ENABLED";
    public static final String MODULE_DISABLED = "MODULE_DISABLED";

    public static final String SUBSCRIPTION_PLAN_CREATED = "SUBSCRIPTION_PLAN_CREATED";
    public static final String SCHOOL_PLAN_UPDATED = "SCHOOL_PLAN_UPDATED";

    public static final String ADMIN_USER_CREATED = "ADMIN_USER_CREATED";
    public static final String ADMIN_PASSWORD_RESET = "ADMIN_PASSWORD_RESET";

    public static final String SUPPORT_IMPERSONATION_STARTED = "SUPPORT_IMPERSONATION_STARTED";
    public static final String SUPPORT_IMPERSONATION_ENDED = "SUPPORT_IMPERSONATION_ENDED";

    public static final String CONFIG_OVERRIDE_CREATED = "CONFIG_OVERRIDE_CREATED";
    public static final String CONFIG_OVERRIDE_UPDATED = "CONFIG_OVERRIDE_UPDATED";

    public static final String GLOBAL_SETTINGS_UPDATED = "GLOBAL_SETTINGS_UPDATED";

    private AuditActions() {
        // Utility class
    }
}
