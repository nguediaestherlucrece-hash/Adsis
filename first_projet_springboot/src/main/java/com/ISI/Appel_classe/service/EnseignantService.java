package com.ISI.Appel_classe.service;

import com.ISI.Appel_classe.dto.request.EnseignantCreateDTO;
import com.ISI.Appel_classe.dto.request.EnseignantUpdateDTO;
import com.ISI.Appel_classe.dto.response.EnseignantDTO;

import java.util.List;

/**
 * Service pour la gestion des enseignants
 */
public interface EnseignantService {

    List<EnseignantDTO> getAllEnseignants();

    EnseignantDTO getEnseignantById(Long id);

    EnseignantDTO getEnseignantByEmail(String email);

    List<EnseignantDTO> getEnseignantsBySpecialite(String specialite);

    List<EnseignantDTO> getEnseignantsByGrade(String grade);

    EnseignantDTO createEnseignant(EnseignantCreateDTO dto);

    EnseignantDTO updateEnseignant(Long id, EnseignantUpdateDTO dto);

    void deleteEnseignant(Long id);

    EnseignantDTO assignerCours(Long enseignantId, List<Long> coursIds);
}
