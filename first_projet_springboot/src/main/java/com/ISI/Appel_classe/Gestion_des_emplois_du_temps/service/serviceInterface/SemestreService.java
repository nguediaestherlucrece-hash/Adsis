package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.SemestreUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SemestreDTO;

import java.util.List;

public interface SemestreService {

    List<SemestreDTO> getAllSemestres();

    SemestreDTO getSemestreById(Long id);

    SemestreDTO getSemestreByCode(String idSemestre);

    SemestreDTO createSemestre(SemestreCreateDTO dto);

    SemestreDTO updateSemestre(Long id, SemestreUpdateDTO dto);

    void deleteSemestre(Long id);

    SemestreDTO activerSemestre(Long id);

    List<SemestreDTO> getSemestresByAnnee(Long anneeId);
}
