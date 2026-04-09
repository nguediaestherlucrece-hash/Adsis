package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

/**
 * DTO pour la mise à jour d'un administrateur
 * Tous les champs sont optionnels
 */
@Data
public class AdministrateurUpdateDTO {

    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private Statut statut;
    private Set<String> roleNames;

    // Permissions retirées
    private Set<String> removedPermissions;
}
