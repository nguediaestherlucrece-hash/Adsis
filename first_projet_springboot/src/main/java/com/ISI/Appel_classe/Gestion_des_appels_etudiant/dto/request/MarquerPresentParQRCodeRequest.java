package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarquerPresentParQRCodeRequest {

    @NotNull(message = "Le token QR est obligatoire")
    private String qrToken;

    @NotNull(message = "L'ID de la séance est obligatoire")
    private Long seanceId;

    private boolean retard;

    private String localisation;
}
