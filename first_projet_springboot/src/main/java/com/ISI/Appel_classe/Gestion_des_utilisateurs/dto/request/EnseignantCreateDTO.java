package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * DTO pour la création d'un enseignant
 * Contient TOUS les champs de Utilisateur + les champs spécifiques à Enseignant
 */
@Data
public class EnseignantCreateDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private Statut statut;
    private Set<String> roleNames; // Si vide, "ENSEIGNANT" par défaut

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    /**
     * IDs des cours que l'enseignant enseigne (ManyToMany)
     */
    private List<Long> coursIds;

    // Permissions retirées
    private Set<String> removedPermissions;
}
