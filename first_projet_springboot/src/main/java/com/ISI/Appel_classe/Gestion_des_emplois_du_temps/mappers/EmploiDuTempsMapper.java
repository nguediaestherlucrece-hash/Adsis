package com.ISI.Appel_classe.Gestion_des_emplois_du_temps.mappers;

import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.dto.response.EmploiDuTempsDTO;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.EmploiDuTemps;
import com.ISI.Appel_classe.Gestion_des_emplois_du_temps.model.Semestre;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Classe;
import org.springframework.stereotype.Component;

@Component
public class EmploiDuTempsMapper {

    public EmploiDuTempsDTO toDTO(EmploiDuTemps emploiDuTemps) {
        if (emploiDuTemps == null) return null;

        EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
        dto.setId(emploiDuTemps.getId());
        dto.setIdEmploi(emploiDuTemps.getIdEmploi());
        dto.setDateDebut(emploiDuTemps.getDateDebut());
        dto.setDateFin(emploiDuTemps.getDateFin());

        Semestre semestre = emploiDuTemps.getSemestre();
        if (semestre != null) {
            dto.setClasseId(semestre.getId());
        }

        Classe classe = emploiDuTemps.getClasse();
        if (classe != null) {
            dto.setClasseId(classe.getId());
        }

        AssistantPedagogique gestionnaire = emploiDuTemps.getGestionnaire();
        if (gestionnaire != null) {
            dto.setGestionnaireId(gestionnaire.getId());
        }

        return dto;
    }
}
