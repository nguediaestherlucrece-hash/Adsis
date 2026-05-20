package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsencePourJustificationDTO {
    private Long      id;
    private String    matiere;
    private LocalDate date;
    /** null = pas de justificatif | EN_ATTENTE | ACCEPTE | REJETE */
    private String    justificatifStatut;
}
