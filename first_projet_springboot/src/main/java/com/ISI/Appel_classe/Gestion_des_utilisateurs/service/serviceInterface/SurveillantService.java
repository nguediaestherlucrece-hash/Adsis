package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SurveillantUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SurveillantDTO;

import java.util.List;

public interface SurveillantService {

    List<SurveillantDTO> getAllSurveillants();

    SurveillantDTO getSurveillantById(Long id);

    SurveillantDTO getSurveillantByEmail(String email);

    List<SurveillantDTO> getSurveillantsByAffectation(String affectation);

    SurveillantDTO createSurveillant(SurveillantCreateDTO dto);

    SurveillantDTO updateSurveillant(Long id, SurveillantUpdateDTO dto);

    void deleteSurveillant(Long id);

    SurveillantDTO assignSurveillantToFiliere(Long surveillantId, Long filiereId);

    java.util.List<SurveillantDTO> getSurveillantsByFiliere(Long filiereId);
}
