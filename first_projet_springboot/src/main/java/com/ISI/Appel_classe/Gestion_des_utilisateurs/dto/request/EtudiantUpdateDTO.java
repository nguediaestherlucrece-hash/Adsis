package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

/**
 * DTO pour la mise à jour d'un étudiant
 * Contient TOUS les champs modifiables de Utilisateur + les champs spécifiques
 * à Etudiant
 */
@Data
public class EtudiantUpdateDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private String nom;
    private String prenom;
    private String email;
    private Statut statut; // ACTIF, INACTIF

    /**
     * Noms des rôles à assigner
     * Si fourni, remplace complètement les rôles existants
     */
    private Set<String> roleNames;

    // Le mot de passe n'est PAS modifiable via ce DTO
    // (il faudrait un endpoint dédié avec ancien/nouveau password)

    // ========================================
    // CHAMPS SPÉCIFIQUES À ETUDIANT
    // ========================================

    private String matricule;
    private String dateNaissance;
    private String telephone;

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    private Long classeId; // Permet de changer de classe
}
