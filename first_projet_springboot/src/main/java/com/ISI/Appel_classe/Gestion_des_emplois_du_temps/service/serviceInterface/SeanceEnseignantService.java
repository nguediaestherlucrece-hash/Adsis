package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

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
}
