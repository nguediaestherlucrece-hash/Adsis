package com.ISI.Appel_classe.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateRolePermissionsDTO {
    private Set<String> permissions;
}
