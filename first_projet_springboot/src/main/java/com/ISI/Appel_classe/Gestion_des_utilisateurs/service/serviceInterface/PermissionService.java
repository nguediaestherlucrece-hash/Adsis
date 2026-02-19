package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.PermissionDTO;

import java.util.List;

public interface PermissionService {
    List<PermissionDTO> getAllPermissions();

    PermissionDTO getPermissionById(Long id);

    void initializeDefaultPermissions();
}
