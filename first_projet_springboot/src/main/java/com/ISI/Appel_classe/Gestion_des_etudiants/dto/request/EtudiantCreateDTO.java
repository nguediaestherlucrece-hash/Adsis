package com.ISI.Appel_classe.Gestion_des_etudiants.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

/**
 * DTO pour la création d'un étudiant
 * Contient TOUS les champs de Utilisateur + les champs spécifiques à Etudiant
 */
@Data
public class EtudiantCreateDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Statut statut; // ACTIF, INACTIF

    /**
     * Noms des rôles à assigner (ex: ["ETUDIANT"])
     * Si vide ou null, le rôle "ETUDIANT" sera assigné par défaut
     */
    private Set<String> roleNames;

    // enabled et accountNonLocked seront à true par défaut
    // premiereConnexion sera à false par défaut
    // createdAt sera généré automatiquement

    // ========================================
    // CHAMPS SPÉCIFIQUES À ETUDIANT
    // ========================================

    private String matricule; // Matricule unique de l'étudiant
    private String dateNaissance; // Format: "YYYY-MM-DD" ou autre selon besoin
    private String telephone; // Numéro de téléphone

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    private Long classeId; // ID de la classe à laquelle assigner l'étudiant
}
