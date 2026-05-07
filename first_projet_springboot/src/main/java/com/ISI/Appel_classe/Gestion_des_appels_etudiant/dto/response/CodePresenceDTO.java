package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodePresenceDTO {

    private Long id;
    private String codePIN;
    private String qrToken;
    private String qrCodeBase64;
    private String type;
    private LocalDateTime dateCreation;
    private LocalDateTime dateExpiration;
    private String statut;
    private boolean utilise;
    private Long etudiantId;
    private Long seanceId;
}
