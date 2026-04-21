package com.ISI.Appel_classe.Gestion_des_appels.dto.response;

import com.ISI.Appel_classe.Gestion_des_appels.dto.request.CreneauPresenceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresenceDTO {
    
    private Long id;
    private String idPresence;
    private LocalDateTime horodatage;
    private String statut;
    private String modeEnregistrement;
    private String localisation;
    private Long etudiantId;
    private String etudiantNom;
    private String etudiantPrenom;
    private String etudiantMatricule;
    private Long seanceId;
    private Long appelId;
    private boolean retard;
    private List<CreneauPresenceDTO> creneaux;
    private int heuresPresentes;
    private int heuresAbsentes;
}
