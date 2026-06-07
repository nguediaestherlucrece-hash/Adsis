package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresenceHistoryDTO {
    private Long presenceId;
    private LocalDate date;
    private String heureDebut;
    private String heureFin;
    private String matiereIntitule;
    private String matiereCode;
    private String statut; // "présent", "absent", "retard", "présent_partiel"
    private double heuresPresentes;
    private double heuresAbsentes;
    private boolean justifiee;
    private String justificatifStatut; // "ACCEPTE", "EN_ATTENTE", "REJETE"
    private String justificatifMotif;
}
