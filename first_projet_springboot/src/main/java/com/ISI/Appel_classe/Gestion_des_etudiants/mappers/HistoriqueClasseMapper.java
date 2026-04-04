package com.ISI.Appel_classe.Gestion_des_etudiants.mappers;

import com.ISI.Appel_classe.Gestion_des_etudiants.dto.response.HistoriqueClasseDTO;
import com.ISI.Appel_classe.Gestion_des_etudiants.model.HistoriqueClasse;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueClasseMapper {

    public HistoriqueClasseDTO toDTO(HistoriqueClasse entity) {
        if (entity == null) return null;

        HistoriqueClasseDTO dto = new HistoriqueClasseDTO();
        dto.setId(entity.getId());
        dto.setEtudiantId(entity.getEtudiant() != null ? entity.getEtudiant().getId() : null);

        // Classe info
        dto.setClasseId(entity.getClasse() != null ? entity.getClasse().getId() : null);
        dto.setClasseNom(entity.getClasse() != null ? entity.getClasse().getNomClasse() : null);

        // Repères temporels
        dto.setAnneeAcademiqueId(entity.getAnneeAcademique() != null ? entity.getAnneeAcademique().getId() : null);
        dto.setAnneeNom(entity.getAnneeAcademique() != null ? entity.getAnneeAcademique().getNomAnneeAcademique() : null);
        dto.setSemestreId(entity.getSemestre() != null ? entity.getSemestre().getId() : null);
        dto.setSemestreNom(entity.getSemestre() != null ? entity.getSemestre().getNomSemestre() : null);

        // Dates
        dto.setDateEntree(entity.getDateEntree());
        dto.setDateSortie(entity.getDateSortie());
        dto.setMotif(entity.getMotif());

        // Les taux de présence seront enrichis par le service
        return dto;
    }
}
