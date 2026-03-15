package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SurveillantDTO {

    // Attributs hérités de Utilisateur
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private Statut statut;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Boolean premiereConnexion;
    private LocalDateTime createdAt;
    private Set<String> roleNames;

    // Permissions
    private Set<String> rolePermissions;
    private Set<String> removedPermissions;
    private Set<String> effectivePermissions;

    // Attributs spécifiques
    private String affectation;

    // Relations
    private Integer nombreSeances;
}
