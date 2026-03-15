package com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.enums.Statut;
import lombok.Data;

import java.util.Set;

@Data
public class UtilisateurUpdateDTO {
    private String nom;
    private String prenom;
    private String email;
    private Set<String> roleNames; // Noms des rôles
    private Statut statut;
    private Set<String> removedPermissions; // Permissions du rôle à retirer pour cet utilisateur
}
