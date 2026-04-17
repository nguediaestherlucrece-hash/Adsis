package com.ISI.Appel_classe.Gestion_des_appels.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
