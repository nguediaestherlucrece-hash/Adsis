package com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemarrerAppelRequest {
    
    @NotNull(message = "L'ID de la séance est obligatoire")
    private Long seanceId;
    
    private String modeAppel; // "QR", "PIN", "MANUEL"
    
    private Integer dureeValiditePIN; // En minutes (par défaut 5)
}
