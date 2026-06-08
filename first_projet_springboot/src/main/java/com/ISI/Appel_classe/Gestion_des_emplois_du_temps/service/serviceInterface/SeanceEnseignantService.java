package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.DashboardEnseignantDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceEnseignantDTO;

import java.time.LocalDate;
import java.util.List;

public interface SeanceEnseignantService {
    
    /**
     * Récupère les séances d'un enseignant pour une date donnée
     */
    List<SeanceEnseignantDTO> getSeancesEnseignantParDate(Long enseignantId, LocalDate date);
    
    /**
     * Récupère les séances d'un enseignant pour la semaine en cours
     */
    List<SeanceEnseignantDTO> getSeancesEnseignantSemaine(Long enseignantId);
    
    /**
     * Récupère les séances d'un enseignant entre deux dates
     */
    List<SeanceEnseignantDTO> getSeancesEnseignantPeriode(Long enseignantId, LocalDate dateDebut, LocalDate dateFin);

    /**
     * Récupère les données du tableau de bord de l'enseignant
     */
    DashboardEnseignantDTO getDashboardData(Long enseignantId);

    /**
     * Récupère les séances assignées à un surveillant entre deux dates
     */
    List<SeanceEnseignantDTO> getSeancesSurveillantParDate(Long surveillantId, LocalDate date);

    /**
     * Récupère les séances assignées à un surveillant entre deux dates
     */
    List<SeanceEnseignantDTO> getSeancesSurveillantPeriode(Long surveillantId, LocalDate dateDebut, LocalDate dateFin);
}
