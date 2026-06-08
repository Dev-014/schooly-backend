package com.school.erp.security;

public final class AuthContextHolder {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    private AuthContextHolder() {
    }

    public static void set(AuthenticatedUser authenticatedUser) {
        CURRENT_USER.set(authenticatedUser);
    }

    public static AuthenticatedUser get() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
