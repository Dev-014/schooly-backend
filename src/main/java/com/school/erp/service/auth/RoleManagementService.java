package com.school.erp.service.auth;

import com.school.erp.dto.auth.*;
import com.school.erp.entity.auth.PermissionDefinition;
import com.school.erp.entity.auth.Role;
import com.school.erp.entity.auth.RoleArchetype;
import com.school.erp.entity.auth.RolePermission;
import com.school.erp.repository.auth.PermissionDefinitionRepository;
import com.school.erp.repository.auth.RolePermissionRepository;
import com.school.erp.repository.auth.RoleRepository;
import com.school.erp.repository.auth.UserRoleMappingRepository;
import com.school.erp.repository.PlatformModuleRepository;
import com.school.erp.entity.PlatformModule;
import com.school.erp.entity.SchoolModuleAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleManagementService {

    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionDefinitionRepository permissionDefinitionRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final com.school.erp.repository.SchoolModuleAccessRepository moduleAccessRepository;
    private final PlatformModuleRepository platformModuleRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> getRoles(Long schoolId) {
        List<Role> roles = roleRepository.findBySchoolIdOrSystemRoles(schoolId);
        
        return roles.stream()
                .filter(role -> role.getArchetype() != RoleArchetype.SUPER_ADMIN)
                .map(role -> {
            RoleDto dto = new RoleDto();
            dto.setId(role.getId());
            dto.setName(role.getName());
            dto.setDescription(role.getDescription());
            dto.setSystemRole(role.isSystemRole());
            
            int userCount = userRoleMappingRepository.findBySchoolIdAndRoleIdAndIsActiveTrue(schoolId, role.getId()).size();
            dto.setUserCount(userCount);
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleDto> getSystemRoles() {
        List<Role> roles = roleRepository.findBySchoolIdOrSystemRoles(null);
        
        return roles.stream().filter(Role::isSystemRole).map(role -> {
            RoleDto dto = new RoleDto();
            dto.setId(role.getId());
            dto.setName(role.getName());
            dto.setDescription(role.getDescription());
            dto.setSystemRole(true);
            dto.setUserCount(0); // System role user count could be globally aggregated if needed, but 0 is fine for now
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionGroupDto> getRolePermissions(Long schoolId, String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Long targetSchoolId = role.isSystemRole() ? null : schoolId;

        List<PermissionDefinition> allDefs = permissionDefinitionRepository.findAll();
        List<RolePermission> grantedPerms = rolePermissionRepository.findBySchoolIdAndRoleId(targetSchoolId, roleId);
        
        Map<String, RolePermission> grantedMap = grantedPerms.stream()
                .collect(Collectors.toMap(rp -> rp.getPermission().getPermissionKey(), rp -> rp, (existing, replacement) -> existing));

        Map<String, List<PermissionDefinitionDto>> moduleMap = new HashMap<>();

        Set<String> enabledModules = null;
        if (schoolId != null) {
            List<SchoolModuleAccess> accessList = moduleAccessRepository.findBySchoolId(schoolId);
            enabledModules = platformModuleRepository.findAll().stream()
                .filter(m -> {
                    Optional<SchoolModuleAccess> accessOpt = accessList.stream()
                            .filter(a -> a.getModule().getId().equals(m.getId()))
                            .findFirst();
                    if (accessOpt.isPresent()) {
                        return Boolean.TRUE.equals(accessOpt.get().getEnabled());
                    }
                    return m.isDefault();
                })
                .map(PlatformModule::getCode)
                .collect(Collectors.toSet());
        }

        for (PermissionDefinition def : allDefs) {
            if (enabledModules != null && !enabledModules.contains(def.getModuleKey())) {
                continue;
            }
            PermissionDefinitionDto dto = new PermissionDefinitionDto();
            dto.setId(def.getId());
            dto.setKey(def.getPermissionKey());
            dto.setName(def.getName());
            dto.setDescription(def.getDescription());
            dto.setSensitive(def.isSensitive());
            dto.setAvailableScopes(def.getSupportedScopeTypes());
            
            RolePermission granted = grantedMap.get(def.getPermissionKey());
            if (granted != null) {
                dto.setGranted(true);
                dto.setScope(granted.getScopeType());
            } else {
                dto.setGranted(false);
                dto.setScope(def.getSupportedScopeTypes() != null && !def.getSupportedScopeTypes().isEmpty() 
                             ? def.getSupportedScopeTypes().get(0) : "SCHOOL");
            }
            
            moduleMap.computeIfAbsent(def.getModuleKey(), k -> new ArrayList<>()).add(dto);
        }

        List<PermissionGroupDto> groups = new ArrayList<>();
        for (Map.Entry<String, List<PermissionDefinitionDto>> entry : moduleMap.entrySet()) {
            PermissionGroupDto group = new PermissionGroupDto();
            group.setModule(entry.getKey());
            group.setPermissions(entry.getValue());
            groups.add(group);
        }
        
        return groups;
    }

    @Transactional
    public void updateRolePermissions(Long schoolId, String roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .filter(r -> r.isSystemRole() || r.getSchoolId().equals(schoolId))
                .orElseThrow(() -> new IllegalArgumentException("Role not found or access denied"));
        Set<String> enabledModules = null;
        if (schoolId != null) {
            if (role.isSystemRole()) {
                throw new com.school.erp.exception.ForbiddenException("System Roles cannot be modified by School Administrators");
            }
            List<SchoolModuleAccess> accessList = moduleAccessRepository.findBySchoolId(schoolId);
            enabledModules = platformModuleRepository.findAll().stream()
                .filter(m -> {
                    Optional<SchoolModuleAccess> accessOpt = accessList.stream()
                            .filter(a -> a.getModule().getId().equals(m.getId()))
                            .findFirst();
                    if (accessOpt.isPresent()) {
                        return Boolean.TRUE.equals(accessOpt.get().getEnabled());
                    }
                    return m.isDefault();
                })
                .map(PlatformModule::getCode)
                .collect(Collectors.toSet());
        }

        List<RolePermission> currentPerms = rolePermissionRepository.findBySchoolIdAndRoleId(schoolId, roleId);
        rolePermissionRepository.deleteAll(currentPerms);
        
        List<RolePermission> newPerms = new ArrayList<>();
        for (UpdateRolePermissionsRequest.PermissionUpdateDto update : request.getPermissions()) {
            if (update.isGranted()) {
                PermissionDefinition def = permissionDefinitionRepository.findByPermissionKey(update.getPermissionKey())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission key: " + update.getPermissionKey()));
                        
                if (enabledModules != null && !enabledModules.contains(def.getModuleKey())) {
                    throw new com.school.erp.exception.ForbiddenException("Cannot assign permission from disabled module: " + def.getModuleKey());
                }
                
                RolePermission rp = new RolePermission();
                rp.setSchoolId(schoolId);
                rp.setRole(role);
                rp.setPermission(def);
                rp.setScopeType(update.getScope());
                newPerms.add(rp);
            }
        }
        
        rolePermissionRepository.saveAll(newPerms);
    }
    
    @Transactional
    public void updateSystemRolePermissions(String roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .filter(Role::isSystemRole)
                .orElseThrow(() -> new IllegalArgumentException("System Role not found"));
                
        List<RolePermission> currentPerms = rolePermissionRepository.findBySchoolIdAndRoleId(null, roleId);
        rolePermissionRepository.deleteAll(currentPerms);
        
        List<RolePermission> newPerms = new ArrayList<>();
        for (UpdateRolePermissionsRequest.PermissionUpdateDto update : request.getPermissions()) {
            if (update.isGranted()) {
                PermissionDefinition def = permissionDefinitionRepository.findByPermissionKey(update.getPermissionKey())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown permission key: " + update.getPermissionKey()));
                        
                RolePermission rp = new RolePermission();
                rp.setSchoolId(null);
                rp.setRole(role);
                rp.setPermission(def);
                rp.setScopeType(update.getScope());
                newPerms.add(rp);
            }
        }
        
        rolePermissionRepository.saveAll(newPerms);
    }
    
    @Transactional
    public RoleDto createRole(Long schoolId, CreateRoleRequest request) {
        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setSchoolId(schoolId);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSystemRole(false);
        role.setActive(true);
        role.setArchetype(RoleArchetype.STAFF);
        role = roleRepository.save(role);
        
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemRole(false);
        dto.setUserCount(0);
        return dto;
    }

    @Transactional
    public RoleDto createSystemRole(CreateRoleRequest request) {
        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setSchoolId(null);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSystemRole(true);
        role.setActive(true);
        role.setArchetype(RoleArchetype.STAFF);
        role = roleRepository.save(role);
        
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemRole(true);
        dto.setUserCount(0);
        return dto;
    }

    @Transactional
    public void seedDefaultRolesForSchool(Long schoolId) {
        // Implementation emptied. 
        // We now rely purely on the global System Roles (school_id IS NULL).
        // RoleSyncService handles assigning these global roles to users automatically.
    }
}
