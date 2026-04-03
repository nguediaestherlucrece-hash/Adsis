package com.ISI.Appel_classe.Gestion_des_etudiants.dto.response;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO de réponse pour un étudiant
 * Contient TOUS les champs de Utilisateur + les champs spécifiques à Etudiant
 */
@Data
public class EtudiantDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    // Le password n'est JAMAIS retourné dans les réponses API pour sécurité
    private Statut statut;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private Boolean premiereConnexion;
    private LocalDateTime createdAt;

    /**
     * Noms des rôles de l'étudiant
     */
    private Set<String> roleNames;

    // ========================================
    // CHAMPS SPÉCIFIQUES À ETUDIANT
    // ========================================

    private String matricule;
    private String dateNaissance;
    private String telephone;

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    /**
     * Informations de la classe de l'étudiant
     */
    private Long classeId;

    /**
     * Nombre de présences enregistrées (optionnel)
     */
    private Integer nombrePresences;
}
