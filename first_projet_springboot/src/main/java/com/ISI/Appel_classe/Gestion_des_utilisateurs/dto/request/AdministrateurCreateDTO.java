package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

/**
 * DTO pour la création d'un administrateur
 * Contient TOUS les champs de Utilisateur
 */
@Data
public class AdministrateurCreateDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private Statut statut;
    private Set<String> roleNames; // Si vide, "ADMINISTRATEUR" par défaut

    // Permissions retirées
    private Set<String> removedPermissions;
}
