package com.school.erp.dto.auth;

import com.school.erp.entity.auth.RoleArchetype;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDto {
    private String roleId;
    private String roleName;
    private RoleArchetype archetype;
}
