package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsenceResumeeDTO {
    private Long      id;
    private String    matiere;
    private LocalDate date;
    private boolean   justifiee;
    /** null = pas de justificatif | EN_ATTENTE | ACCEPTE | REJETE */
    private String    justificatifStatut;
}
