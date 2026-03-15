package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.service.serviceInterface;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueCreateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.request.AnneeAcademiqueUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.AnneeAcademiqueDTO;

import java.util.List;

public interface AnneeAcademiqueService {

    List<AnneeAcademiqueDTO> getAllAnnees();

    AnneeAcademiqueDTO getAnneeById(Long id);

    AnneeAcademiqueDTO getAnneeByCode(String idAnnee);

    AnneeAcademiqueDTO createAnnee(AnneeAcademiqueCreateDTO dto);

    AnneeAcademiqueDTO updateAnnee(Long id, AnneeAcademiqueUpdateDTO dto);

    void deleteAnnee(Long id);
}

