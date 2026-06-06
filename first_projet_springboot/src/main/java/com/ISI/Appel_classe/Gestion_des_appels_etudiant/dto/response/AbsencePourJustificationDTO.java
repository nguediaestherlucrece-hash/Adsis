package com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AbsencePourJustificationDTO {
    private Long      id;
    private String    matiere;
    private LocalDate date;
    /** Heure de début de la séance */
    private String    heureDebutSeance;
    /** Heure de fin de la séance */
    private String    heureFinSeance;
    /** Nombre d'heures effectivement absentes (créneaux marqués false) */
    private int       heuresAbsentes;
    /** Nombre d'heures effectivement présentes (créneaux marqués true) */
    private int       heuresPresentes;
    /** Statut global : absent | présent_partiel | retard */
    private String    statut;
    /** null = pas de justificatif | EN_ATTENTE | ACCEPTE | REJETE */
    private String    justificatifStatut;
}
