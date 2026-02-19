package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.SurveillantDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Surveillant;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SurveillantMapper {

    public SurveillantDTO toDTO(Surveillant surveillant) {
        if (surveillant == null) return null;
        SurveillantDTO dto = new SurveillantDTO();

        dto.setId(surveillant.getId());
        dto.setNom(surveillant.getNom());
        dto.setPrenom(surveillant.getPrenom());
        dto.setEmail(surveillant.getEmail());
        dto.setStatut(surveillant.getStatut());
        dto.setEnabled(surveillant.getEnabled());
        dto.setAccountNonLocked(surveillant.getAccountNonLocked());
        dto.setPremiereConnexion(surveillant.getPremiereConnexion());
        dto.setCreatedAt(surveillant.getCreatedAt());

        Set<String> roleNames = surveillant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        dto.setAffectation(surveillant.getAffectation());

        if (surveillant.getSeances() != null) {
            dto.setNombreSeances(surveillant.getSeances().size());
        }

        return dto;
    }
}
