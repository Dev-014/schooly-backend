package com.school.erp.security;

import com.school.erp.exception.BadRequestException;
import com.school.erp.exception.ForbiddenException;
import com.school.erp.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
public class AuthContextService {

    public AuthenticatedUser getCurrentUserOrNull() {
        return AuthContextHolder.get();
    }

    public AuthenticatedUser requireCurrentUser() {
        AuthenticatedUser authenticatedUser = getCurrentUserOrNull();
        if (authenticatedUser == null) {
            throw new UnauthorizedException("Authentication required");
        }
        return authenticatedUser;
    }

    public Long resolveSchoolId(Long requestedSchoolId) {
        AuthenticatedUser authenticatedUser = getCurrentUserOrNull();
        if (authenticatedUser == null) {
            if (requestedSchoolId == null) {
                throw new BadRequestException("schoolId is required");
            }
            return requestedSchoolId;
        }

        if (requestedSchoolId != null && !requestedSchoolId.equals(authenticatedUser.schoolId())) {
            throw new ForbiddenException("Token does not allow access to the requested school");
        }
        return authenticatedUser.schoolId();
    }

    public void validateSameSchool(Long firstSchoolId, Long secondSchoolId) {
        if (firstSchoolId != null && secondSchoolId != null && !firstSchoolId.equals(secondSchoolId)) {
            throw new BadRequestException("schoolId mismatch between request values");
        }
    }
}
