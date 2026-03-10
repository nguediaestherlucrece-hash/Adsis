package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import org.springframework.stereotype.Component;

@Component
public class EmploiDuTempsMapper {

    public EmploiDuTempsDTO toDTO(EmploiDuTemps entity) {
        if (entity == null) return null;

        EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
        dto.setId(entity.getId());
        dto.setIdEmploi(entity.getIdEmploi());
        dto.setPeriode(entity.getPeriode());
        dto.setCreneaux(entity.getCreneaux());

        Classe classe = entity.getClasse();
        if (classe != null) {
            dto.setClasseId(classe.getId());
            dto.setClasseNom(classe.getNomClasse());
        }

        AssistantPedagogique gestionnaire = entity.getGestionnaire();
        if (gestionnaire != null) {
            dto.setGestionnaireId(gestionnaire.getId());
            dto.setGestionnaireNomComplet(
                    (gestionnaire.getNom() != null ? gestionnaire.getNom() : "") + " " +
                    (gestionnaire.getPrenom() != null ? gestionnaire.getPrenom() : "")
            );
        }

        if (entity.getSeances() != null) {
            dto.setNombreSeances(entity.getSeances().size());
        }

        return dto;
    }
}
