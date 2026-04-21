package com.ISI.Appel_classe.Gestion_des_appels.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnregistrerPresenceRequest {
    
    @NotNull(message = "L'ID de l'étudiant est obligatoire")
    private Long etudiantId;
    
    @NotNull(message = "Le statut est obligatoire")
    private String statut; // "présent", "absent", "retard"
    
    private String modeEnregistrement; // "QR", "PIN", "manuel"
    
    private String localisation;

    private boolean retard; // Flag retard global

    private List<CreneauPresenceDTO> creneaux; // Présence par heure
}
