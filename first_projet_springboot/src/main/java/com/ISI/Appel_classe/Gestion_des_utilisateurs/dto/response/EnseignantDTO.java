package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO de réponse pour un enseignant
 */
@Data
public class EnseignantDTO {

    // ========================================
    // CHAMPS HÉRITÉS DE UTILISATEUR
    // ========================================

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

    // ========================================
    // CHAMPS SPÉCIFIQUES À ENSEIGNANT
    // ========================================

    private String specialite;
    private String grade;

    // ========================================
    // RELATIONS SPÉCIFIQUES
    // ========================================

    /**
     * Cours enseignés (simplifiés)
     */
    private List<CoursSimpleDTO> coursEnseignes;

    /**
     * Nombre de séances
     */
    private Integer nombreSeances;

    /**
     * DTO simplifié pour les cours
     */
    @Data
    public static class CoursSimpleDTO {
        private Long id;
        private String nomCours;
        private String code;
    }
}
