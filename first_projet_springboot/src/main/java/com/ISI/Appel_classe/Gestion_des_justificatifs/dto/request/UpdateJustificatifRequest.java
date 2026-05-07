package com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request;

import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJustificatifRequest {

    private StatutValidation statutValidation;
    private String motifRejet; // Requis si rejeté
}
