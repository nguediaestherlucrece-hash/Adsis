package com.ISI.Appel_classe.dto.response;

import com.ISI.Appel_classe.entity.Statut;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UtilisateurDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private Statut statut;
    private Set<String> roleNames;
    private Boolean premiereConnexion;
    private LocalDateTime createdAt;
}
