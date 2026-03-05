package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.SpecialiteUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SpecialiteDTO;

import java.util.List;

public interface SpecialiteService {

    List<SpecialiteDTO> getAllSpecialites();

    SpecialiteDTO getSpecialiteById(Long id);

    SpecialiteDTO createSpecialite(SpecialiteCreateDTO dto);

    SpecialiteDTO updateSpecialite(Long id, SpecialiteUpdateDTO dto);

    void deleteSpecialite(Long id);
}