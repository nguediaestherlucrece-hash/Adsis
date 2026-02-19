package com.Gestion_des_utilisateurs.dto.request;

import com.Gestion_des_utilisateurs.entity.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class UtilisateurUpdateDTO {
    private String nom;
    private String prenom;
    private String email;
    private Set<String> roleNames; // Noms des rôles
    private Statut statut;
}
