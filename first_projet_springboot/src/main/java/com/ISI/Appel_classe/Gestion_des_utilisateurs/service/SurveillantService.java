package com.Gestion_des_utilisateurs.service;

import com.Gestion_des_utilisateurs.dto.request.SurveillantCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.SurveillantUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.SurveillantDTO;

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
