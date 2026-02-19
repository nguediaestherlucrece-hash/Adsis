package com.Gestion_des_utilisateurs.dto.request;

import com.Gestion_des_utilisateurs.entity.Statut;
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
}
