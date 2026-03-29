package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.SeanceDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.CoursUE;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Seance;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Surveillant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SeanceMapper {

    public SeanceDTO toDTO(Seance seance) {
        if (seance == null) { return null; }

        SeanceDTO dto = new SeanceDTO();
        dto.setId(seance.getId());
        dto.setIdSeance(seance.getIdSeance());
        dto.setDate(seance.getDate());
        dto.setHeureDebut(seance.getHeureDebut());
        dto.setHeureFin(seance.getHeureFin());
        dto.setStatut(seance.isStatut());

        dto.setCoursId(seance.getCours() != null ? seance.getCours().getId() : null);
        dto.setEnseignantId(seance.getEnseignant() != null ? seance.getEnseignant().getId() : null);
        dto.setEmploiDuTempsId(seance.getEmploiDuTemps() != null ? seance.getEmploiDuTemps().getId() : null);
        dto.setCouleur(seance.getCouleur());
        dto.setGroupId(seance.getGroupId());

        return dto;
    }
}
