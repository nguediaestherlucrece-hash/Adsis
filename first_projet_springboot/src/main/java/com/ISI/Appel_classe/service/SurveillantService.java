package com.ISI.Appel_classe.service;

import com.ISI.Appel_classe.dto.request.SurveillantCreateDTO;
import com.ISI.Appel_classe.dto.request.SurveillantUpdateDTO;
import com.ISI.Appel_classe.dto.response.SurveillantDTO;

import java.util.List;

public interface SurveillantService {

    List<SurveillantDTO> getAllSurveillants();

    SurveillantDTO getSurveillantById(Long id);

    SurveillantDTO getSurveillantByEmail(String email);

    List<SurveillantDTO> getSurveillantsByAffectation(String affectation);

    SurveillantDTO createSurveillant(SurveillantCreateDTO dto);

    SurveillantDTO updateSurveillant(Long id, SurveillantUpdateDTO dto);

    void deleteSurveillant(Long id);
}
