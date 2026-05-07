package com.ISI.Appel_classe.Gestion_des_appels_etudiant.mappers;

import com.ISI.Appel_classe.Gestion_des_appels_etudiant.dto.response.PresenceEtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_appels_enseignant.dto.request.CreneauPresenceDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Presence;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class PresenceEtudiantMapper {

    public PresenceEtudiantDTO toDTO(Presence presence) {
        if (presence == null) {
            return null;
        }

        PresenceEtudiantDTO dto = new PresenceEtudiantDTO();
        dto.setId(presence.getId());
        dto.setIdPresence(presence.getIdPresence());
        dto.setHorodatage(presence.getHorodatage());
        dto.setStatut(presence.getStatut());
        dto.setModeEnregistrement(presence.getModeEnregistrement());
        dto.setLocalisation(presence.getLocalisation());
        dto.setRetard(presence.isRetard());
        dto.setHeuresPresentes(presence.getHeuresPresentes());
        dto.setHeuresAbsentes(presence.getHeuresAbsentes());

        if (presence.getEtudiant() != null) {
            dto.setEtudiantId(presence.getEtudiant().getId());
        }

        if (presence.getSeance() != null) {
            dto.setSeanceId(presence.getSeance().getId());
        }

        if (presence.getAppel() != null) {
            dto.setAppelId(presence.getAppel().getId());
        }

        if (presence.getPresencesHoraires() != null && !presence.getPresencesHoraires().isEmpty()) {
            dto.setCreneaux(presence.getPresencesHoraires().stream()
                    .map(ph -> {
                        CreneauPresenceDTO c = new CreneauPresenceDTO();
                        c.setHeureDebut(ph.getHeureDebut());
                        c.setHeureFin(ph.getHeureFin());
                        c.setPresent(ph.isPresent());
                        return c;
                    })
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
