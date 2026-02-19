package com.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolePermissionsDTO {
    private Set<String> permissions;
}
