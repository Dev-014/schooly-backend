package com.school.erp.security;

import com.school.erp.entity.UserRole;
import com.school.erp.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

/**
 * Interceptor that enforces @RoleRequired annotations on controller methods.
 * Checks the current user's role from AuthContextHolder against the required roles.
 */
@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RoleRequired roleRequired = handlerMethod.getMethodAnnotation(RoleRequired.class);
        if (roleRequired == null) {
            return true; // No role restriction on this method
        }

        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (currentUser == null) {
            throw new ForbiddenException("Authentication required to access this resource");
        }

        UserRole[] allowedRoles = roleRequired.value();
        boolean hasRole = Arrays.asList(allowedRoles).contains(currentUser.role());

        if (!hasRole) {
            throw new ForbiddenException(
                    "Access denied. Required role(s): " + Arrays.toString(allowedRoles)
                            + ", your role: " + currentUser.role()
            );
        }

        return true;
    }
}
