package com.ISI.Appel_classe.Gestion_des_statistiques_de_cours.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassMinusReportDTO {
    private Long etudiantId;
    private String matricule;
    private String nom;
    private String prenom;
    // Map of CoursUE ID to total hours of unjustified absence
    private Map<Long, Double> heuresInjustifieesParCours;
    // Map of CoursUE ID to completed hours
    private Map<Long, Double> heuresEffectueesParCours;
    // Map of CoursUE ID to boolean if penalty threshold (>10%) is exceeded
    private Map<Long, Boolean> aMalusParCours;
}
