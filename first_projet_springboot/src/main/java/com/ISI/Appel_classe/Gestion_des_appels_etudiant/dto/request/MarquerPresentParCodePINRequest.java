package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarquerPresentParCodePINRequest {

    @NotNull(message = "Le code PIN est obligatoire")
    private String codePIN;

    @NotNull(message = "L'ID de la séance est obligatoire")
    private Long seanceId;

    private boolean retard;

    private String localisation;
}
