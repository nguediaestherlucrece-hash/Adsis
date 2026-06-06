package com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppelDTO {
    
    private Long id;
    private String idAppel;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String statut;
    private Long seanceId;
    private String qrCodeBase64; // QR Code en base64
    private String codePIN;
    private LocalDateTime expirationPIN;
    private int numeroAppel;   // rang de cet appel dans la séance (1, 2, 3…)
    private int totalAppels;   // nombre total d’appels pour la séance
}
