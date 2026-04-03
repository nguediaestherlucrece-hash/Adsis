package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class AssistantPedagogiqueCreateDTO {

    // Attributs hérités
    private String nom;
    private String prenom;
    private String email;
    private String matricule;
    private Statut statut;
    private Set<String> roleNames; // Si vide, "ASSISTANT_PEDAGOGIQUE" par défaut

    // Permissions retirées
    private Set<String> removedPermissions;

    private  Long filiereId;
}
