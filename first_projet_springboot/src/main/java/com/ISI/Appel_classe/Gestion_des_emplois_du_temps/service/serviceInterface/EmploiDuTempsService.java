package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.EmploiDuTempsUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;

import java.util.List;

public interface EmploiDuTempsService {

    List<EmploiDuTempsDTO> getAllEmplois();

    EmploiDuTempsDTO getEmploiById(Long id);

    EmploiDuTempsDTO getEmploiByCode(String idEmploi);

    EmploiDuTempsDTO createEmploi(EmploiDuTempsCreateDTO dto);

    EmploiDuTempsDTO updateEmploi(Long id, EmploiDuTempsUpdateDTO dto);

    void deleteEmploi(Long id);
}
