package com.Gestion_des_utilisateurs.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
}
