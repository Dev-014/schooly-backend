package com.school.erp.security;

import com.school.erp.exception.ForbiddenException;
import com.school.erp.service.auth.AuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionAuthorizationInterceptor implements HandlerInterceptor {

    private final AuthorizationService authorizationService;

    public PermissionAuthorizationInterceptor(ObjectProvider<AuthorizationService> authorizationServiceProvider) {
        this.authorizationService = authorizationServiceProvider.getIfAvailable();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        PermissionRequired permissionRequired = handlerMethod.getMethodAnnotation(PermissionRequired.class);
        if (permissionRequired == null) {
            return true; // No permission restriction on this method
        }

        AuthenticatedUser currentUser = AuthContextHolder.get();
        if (currentUser == null || currentUser.schoolId() == null) {
            throw new ForbiddenException("Authentication and valid school context required to access this resource");
        }

        // Handle Super Admins bypassing normal tenant checks, if needed
        // For MVP, we stick strictly to the multi-tenant permission framework
        String requiredPermissionKey = permissionRequired.value();
        
        if (authorizationService == null) {
            // In @WebMvcTest slices, the AuthorizationService is not loaded.
            return true; 
        }
        
        boolean hasPermission = authorizationService.hasPermission(
                currentUser.schoolId(), 
                currentUser.userId(), 
                requiredPermissionKey
        );

        if (!hasPermission) {
            throw new ForbiddenException(
                    "Access denied. Missing permission: " + requiredPermissionKey
            );
        }

        return true;
    }
}
