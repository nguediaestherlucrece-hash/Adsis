package com.Gestion_des_utilisateurs.service;

import com.Gestion_des_utilisateurs.dto.response.PermissionDTO;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();

    PermissionDTO getPermissionById(Long id);

    void initializeDefaultPermissions();
}
