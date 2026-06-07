package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUEMinusStatsDTO {
    private Long coursId;
    private String codeUE;
    private String intitule;
    private Integer credit;
    private double volumeHoraireSemestre; // credit * 15
    private double heuresEffectuees; // total completed hours of this subject
    private double heuresAbsenceInjustifiee; // student's unjustified absences hours
    private boolean atteintTotaliteHeures; // true if heuresEffectuees >= volumeHoraireSemestre
    private boolean aMalusCC; // true if unjustified hours > 10% of completed hours
    private int pointsMalusCC; // 1 if aMalusCC is true, else 0
    private String admisSN; // "sn oui" or "sn non" (unjustified hours > 33% of completed hours means "sn non")
}
