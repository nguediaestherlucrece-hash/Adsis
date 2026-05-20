package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsenceDetailDTO {
    private Long      id;
    private String    matiere;
    private LocalDate date;
    private String    heureDebut;
    private String    heureFin;
    private String    statut;
    private boolean   justifiee;
    private Long      justificatifId;
    /** null = pas de justificatif | EN_ATTENTE | ACCEPTE | REJETE */
    private String    justificatifStatut;
}
