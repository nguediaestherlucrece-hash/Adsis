package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * DTO pour la mise à jour d'un enseignant
 */
@Data
public class EnseignantUpdateDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private String nom;
    private String prenom;
    private String email;
    private Statut statut;
    private Set<String> roleNames;

    // ========================================
    // CHAMPS SPÉCIFIQUES À ENSEIGNANT
    // ========================================

    private String specialite;
    private String grade;

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    private List<Long> coursIds;

    // Permissions retirées
    private Set<String> removedPermissions;
}
