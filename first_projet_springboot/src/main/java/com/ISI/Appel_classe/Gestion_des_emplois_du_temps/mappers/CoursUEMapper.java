package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.CoursUEDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EnseignantSimpleDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

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
        dto.setSpecialiteNom(cours.getSpecialite() != null ? cours.getSpecialite().getNom() : null);

        if (cours.getEnseignants() != null) {
            dto.setEnseignants(cours.getEnseignants().stream().map(e -> {
                EnseignantSimpleDTO edto = new EnseignantSimpleDTO();
                edto.setId(e.getId());
                edto.setNom(e.getNom());
                edto.setPrenom(e.getPrenom());
                return edto;
            }).collect(Collectors.toList()));
        }

        return dto;
    }
}
