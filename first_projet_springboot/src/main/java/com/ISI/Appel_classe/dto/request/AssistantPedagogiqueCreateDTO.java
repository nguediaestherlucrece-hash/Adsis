package com.ISI.Appel_classe.dto.request;

import com.ISI.Appel_classe.entity.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class AssistantPedagogiqueCreateDTO {

    // Attributs hérités
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Statut statut;
    private Set<String> roleNames; // Si vide, "ASSISTANT_PEDAGOGIQUE" par défaut

    // Attributs spécifiques
    private String departement; // Ex: "Département Informatique", "Département Sciences"
}
