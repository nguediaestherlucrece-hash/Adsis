package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SeanceUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceDTO;

import java.util.List;

public interface SeanceService {

    List<SeanceDTO> getAllSeances();

    SeanceDTO getSeanceById(Long id);

    SeanceDTO getSeanceByCode(String idSeance);

    SeanceDTO createSeance(SeanceCreateDTO dto);

    SeanceDTO updateSeance(Long id, SeanceUpdateDTO dto);

    void deleteSeance(Long id);

    List<SeanceDTO> getSeancesByEmploiDuTempsId(Long emploiDuTempsId);
}
