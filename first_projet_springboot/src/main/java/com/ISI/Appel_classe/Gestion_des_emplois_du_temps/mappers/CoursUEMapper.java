package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import org.springframework.stereotype.Component;

@Component
public class CoursUEMapper {

    public CoursUEDTO toDTO(CoursUE entity) {
        if (entity == null) return null;

        CoursUEDTO dto = new CoursUEDTO();
        dto.setId(entity.getId());
        dto.setIdCours(entity.getIdCours());
        dto.setCodeUE(entity.getCodeUE());
        dto.setIntitule(entity.getIntitule());
        dto.setVolumeHoraire(entity.getVolumeHoraire());

        if (entity.getClasses() != null) {
            dto.setNombreClasses(entity.getClasses().size());
        }

        if (entity.getEnseignants() != null) {
            dto.setNombreEnseignants(entity.getEnseignants().size());
        }

        return dto;
    }
}
