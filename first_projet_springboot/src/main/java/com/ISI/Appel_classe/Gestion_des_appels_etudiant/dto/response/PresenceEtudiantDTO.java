package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.CreneauPresenceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceEtudiantDTO {

    private Long id;
    private String idPresence;
    private java.time.LocalDateTime horodatage;
    private String statut; // "présent", "absent", "retard", "présent_partiel"
    private String modeEnregistrement; // "QR", "PIN", "manuel"
    private String localisation;
    private Long etudiantId;
    private Long seanceId;
    private Long appelId;
    private boolean retard;
    private List<com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.CreneauPresenceDTO> creneaux;
    private int heuresPresentes;
    private int heuresAbsentes;
    /** Message lisible : "\u26a0\ufe0f Arrivé en retard \u00b7 Enregistré \u00e0 08h25 \u00b7 1h absent(e)" */
    private String messageStatut;
    /** Heure de réponse formatée "HH:mm" pour affichage étudiant */
    private String heureEnregistrement;
}
