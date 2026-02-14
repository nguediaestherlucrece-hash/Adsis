package com.ISI.Appel_classe.service;

import com.ISI.Appel_classe.dto.request.CreateRoleDTO;
import com.ISI.Appel_classe.dto.request.UpdateRolePermissionsDTO;
import com.ISI.Appel_classe.dto.response.RoleDTO;

import java.util.List;

public interface RoleService {
    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO createRole(CreateRoleDTO dto);

    RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsDTO dto);

    void deleteRole(Long id);

    void initializeDefaultRoles();
}
