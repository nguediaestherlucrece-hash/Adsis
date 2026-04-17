package com.ISI.Appel_classe.Gestion_des_appels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantSeanceDTO {
    
    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String statut; // "présent", "absent", "retard", "non_enregistré"
    private Long presenceId; // Si déjà enregistré
}
