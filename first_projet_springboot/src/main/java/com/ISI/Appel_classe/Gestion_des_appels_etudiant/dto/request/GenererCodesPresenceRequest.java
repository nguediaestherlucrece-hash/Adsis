package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenererCodesPresenceRequest {

    @NotNull(message = "L'ID de la séance est obligatoire")
    private Long seanceId;

    private Integer dureeValiditeMinutes; // Par défaut 5 minutes

    private String type; // "PIN", "QR", "BOTH" (par défaut "BOTH")
}

