package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.CycleUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.CycleDTO;

import java.util.List;

public interface CycleService {
    List<CycleDTO> getAllCycles();

    CycleDTO getCycleById(Long id);

    CycleDTO createCycle(CycleCreateDTO dto);

    CycleDTO updateCycle(Long id, CycleUpdateDTO dto);

    void deleteCycle(Long id);
}

