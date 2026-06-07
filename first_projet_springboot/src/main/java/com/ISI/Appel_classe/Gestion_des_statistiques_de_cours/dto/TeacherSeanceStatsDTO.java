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
public class TeacherSeanceStatsDTO {
    private Long seanceId;
    private LocalDate date;
    private String heureDebut;
    private String heureFin;
    private String codeUE;
    private String intitule;
    private String nomClasse;
    private int totalEtudiants;
    private int presents;
    private int absentsJustifies;
    private int absentsNonJustifies;
    private int retards;
    private double tauxPresence;
}
