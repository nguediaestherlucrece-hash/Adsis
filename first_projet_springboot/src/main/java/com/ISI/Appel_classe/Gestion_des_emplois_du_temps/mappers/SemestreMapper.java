package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SemestreDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import org.springframework.stereotype.Component;

@Component
public class SemestreMapper {

    public SemestreDTO toDTO(Semestre semestre) {
        if (semestre == null) { return null; }

        SemestreDTO dto = new SemestreDTO();
        dto.setId(semestre.getId());
        dto.setIdSemestre(semestre.getIdSemestre());
        dto.setNomSemestre(semestre.getNomSemestre());
        dto.setStatut(semestre.isStatut());

        dto.setAnneeAcademiqueId(semestre.getAnnee() != null ? semestre.getAnnee().getId() : null);

        return dto;
    }
}

