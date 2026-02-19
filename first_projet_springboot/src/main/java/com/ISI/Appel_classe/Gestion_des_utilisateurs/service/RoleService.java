package com.Gestion_des_utilisateurs.service;

import com.Gestion_des_utilisateurs.dto.request.CreateRoleDTO;
import com.Gestion_des_utilisateurs.dto.request.UpdateRolePermissionsDTO;
import com.Gestion_des_utilisateurs.dto.response.RoleDTO;

import java.util.List;

public interface RoleService {
    List<RoleDTO> getAllRoles();

    RoleDTO getRoleById(Long id);

    RoleDTO createRole(CreateRoleDTO dto);

    RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsDTO dto);

    void deleteRole(Long id);

    void initializeDefaultRoles();
}
