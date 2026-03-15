package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import org.springframework.stereotype.Component;

@Component
public class CoursUEMapper {

    public CoursUEDTO toDTO(CoursUE cours) {
        if (cours == null) return null;

        CoursUEDTO dto = new CoursUEDTO();
        dto.setId(cours.getId());
        dto.setIdCours(cours.getIdCours());
        dto.setCodeUE(cours.getCodeUE());
        dto.setIntitule(cours.getIntitule());
        dto.setVolumeHoraire(cours.getVolumeHoraire());
        dto.setSpecialiteId(cours.getSpecialite() != null ? cours.getSpecialite().getId() : null);

        return dto;
    }
}
