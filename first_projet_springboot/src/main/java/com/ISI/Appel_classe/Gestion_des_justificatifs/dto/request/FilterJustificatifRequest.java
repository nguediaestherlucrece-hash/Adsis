package com.ISI.Appel_classe.Gestion_des_justificatifs.dto.request;

import com.ISI.Appel_classe.Gestion_des_justificatifs.enums.StatutValidation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterJustificatifRequest {

    private Long etudiantId; // Pour assistant: filtrer par étudiant
    private LocalDate dateDebutSoumission;
    private LocalDate dateFinSoumission;
    private LocalDate dateDebutValidation;
    private LocalDate dateFinValidation;
    private StatutValidation statutValidation; // EN_ATTENTE, ACCEPTE, REJETE
    private int page = 0;
    private int size = 20;
}
