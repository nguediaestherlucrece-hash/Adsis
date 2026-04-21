package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardEnseignantDTO {

    private double tauxPresence;              // % moyen de présence
    private int heuresSemaine;                // heures de cours cette semaine
    private int coursAujourdhui;              // nb séances aujourd'hui
    private int etudiantsTotal;               // étudiants uniques dans ses classes
    private SeanceEnseignantDTO prochainCours; // nullable
    private List<SeanceEnseignantDTO> coursJour;
}
