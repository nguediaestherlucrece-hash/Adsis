package com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response;

import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.CreneauPresenceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private boolean retard;
    private List<CreneauPresenceDTO> creneaux; // Créneaux horaires pré-remplis
}
