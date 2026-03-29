package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private Statut statut;
    private Set<String> roleNames;
    private Boolean premiereConnexion;
    private LocalDateTime createdAt;

    // Permissions
    private Set<String> rolePermissions;       // toutes les permissions héritées du rôle
    private Set<String> removedPermissions;    // permissions retirées pour cet utilisateur
    private Set<String> effectivePermissions;  // permissions finales (rôle - retirées)
}
