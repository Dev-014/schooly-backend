package com.school.erp.config;

import com.school.erp.security.PermissionAuthorizationInterceptor;
import com.school.erp.security.RoleAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RoleAuthorizationInterceptor roleAuthorizationInterceptor;
    private final PermissionAuthorizationInterceptor permissionAuthorizationInterceptor;

    public WebMvcConfig(
            RoleAuthorizationInterceptor roleAuthorizationInterceptor,
            PermissionAuthorizationInterceptor permissionAuthorizationInterceptor) {
        this.roleAuthorizationInterceptor = roleAuthorizationInterceptor;
        this.permissionAuthorizationInterceptor = permissionAuthorizationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleAuthorizationInterceptor)
                .addPathPatterns("/api/**", "/auth/me", "/auth/logout");
                
        registry.addInterceptor(permissionAuthorizationInterceptor)
                .addPathPatterns("/api/**", "/auth/me", "/auth/logout");
    }
}
