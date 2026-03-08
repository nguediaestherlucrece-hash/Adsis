package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.NiveauUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.NiveauDTO;

import java.util.List;

public interface NiveauService {

    List<NiveauDTO> getAllNiveaux();

    NiveauDTO getNiveauById(Long id);

    NiveauDTO createNiveau(NiveauCreateDTO dto);

    NiveauDTO updateNiveau(Long id, NiveauUpdateDTO dto);

    void deleteNiveau(Long id);
}
