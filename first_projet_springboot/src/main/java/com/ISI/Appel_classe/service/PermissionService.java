package com.ISI.Appel_classe.service;

import com.ISI.Appel_classe.dto.response.PermissionDTO;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();

    PermissionDTO getPermissionById(Long id);

    void initializeDefaultPermissions();
}
