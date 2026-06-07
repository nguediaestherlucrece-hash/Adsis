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
public class ClassSNAdmissionReportDTO {
    private Long etudiantId;
    private String matricule;
    private String nom;
    private String prenom;
    // Map of CoursUE ID to "sn oui" or "sn non"
    private Map<Long, String> admissionSNParCours;
}
