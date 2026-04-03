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
    private Statut statut;
    private Set<String> roleNames; // Si vide, "SURVEILLANT" par défaut

    // Permissions retirées
    private Set<String> removedPermissions;

    private  Long filiereId;
}
