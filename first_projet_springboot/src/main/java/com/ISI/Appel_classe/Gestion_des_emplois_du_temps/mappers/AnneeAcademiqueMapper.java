package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.AnneeAcademiqueDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.AnneeAcademique;
import org.springframework.stereotype.Component;

@Component
public class AnneeAcademiqueMapper {

    public AnneeAcademiqueDTO toDTO(AnneeAcademique annee) {
        if (annee == null) { return null; }

        AnneeAcademiqueDTO dto = new AnneeAcademiqueDTO();
        dto.setId(annee.getId());
        dto.setIdAnneeAcademique(annee.getIdAnneeAcademique());
        dto.setNomAnneeAcademique(annee.getNomAnneeAcademique());
        dto.setAnneeDebut(annee.getAnneeDebut());
        dto.setAnneeFin(annee.getAnneeFin());
        dto.setActif(annee.isActif());
        dto.setMigrationAutorisee(annee.isMigrationAutorisee());
        dto.setNombreSemestres(annee.getSemestres() != null ? annee.getSemestres().size() : 0);

        return dto;
    }
}
