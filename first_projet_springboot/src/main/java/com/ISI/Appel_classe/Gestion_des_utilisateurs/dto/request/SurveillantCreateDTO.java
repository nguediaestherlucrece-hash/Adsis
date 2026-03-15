package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class SurveillantCreateDTO {

    // Attributs hérités de Utilisateur
    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private String password;
    private Statut statut;
    private Set<String> roleNames; // Si vide, "SURVEILLANT" par défaut

    // Attributs spécifiques à Surveillant
    private String affectation; // Ex: "Bâtiment A", "Département Informatique"

    // Permissions retirées
    private Set<String> removedPermissions;
}
