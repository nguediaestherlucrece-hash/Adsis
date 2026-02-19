package com.Gestion_des_utilisateurs.service;

import com.Gestion_des_utilisateurs.dto.request.EnseignantCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.EnseignantUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.EnseignantDTO;

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
