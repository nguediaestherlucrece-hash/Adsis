package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.FiliereUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.FiliereDTO;

import java.util.List;

public interface FiliereService {

    List<FiliereDTO> getAllFilieres();

    FiliereDTO getFiliereById(Long id);

    FiliereDTO createFiliere(FiliereCreateDTO dto);

    FiliereDTO updateFiliere(Long id, FiliereUpdateDTO dto);

    void deleteFiliere(Long id);
}
