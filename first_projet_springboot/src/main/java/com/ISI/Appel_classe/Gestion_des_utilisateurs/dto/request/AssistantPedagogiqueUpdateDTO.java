package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class AssistantPedagogiqueUpdateDTO {

    private String nom;
    private String prenom;
    private String email;
    private Statut statut;
    private Set<String> roleNames;
    private String departement;

    // Permissions retirées
    private Set<String> removedPermissions;
}
