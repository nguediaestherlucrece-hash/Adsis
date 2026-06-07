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
public class ClassAbsenceReportDTO {
    private Long etudiantId;
    private String matricule;
    private String nom;
    private String prenom;
    // Map of CoursUE ID to total hours of absence
    private Map<Long, Double> heuresAbsencesParCours;
    // Map of CoursUE ID to total hours of justified absence
    private Map<Long, Double> heuresJustifieesParCours;
}
