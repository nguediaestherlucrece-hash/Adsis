package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleDTO {
    private String name;
    private String description;
    private Set<String> permissions;
}
