package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.EnseignantDTO;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Enseignant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EnseignantMapper {

    public EnseignantDTO toDTO(Enseignant enseignant) {
        if (enseignant == null) return null;
        EnseignantDTO dto = new EnseignantDTO();

        dto.setId(enseignant.getId());
        dto.setNom(enseignant.getNom());
        dto.setPrenom(enseignant.getPrenom());
        dto.setEmail(enseignant.getEmail());
        dto.setStatut(enseignant.getStatut());
        dto.setEnabled(enseignant.getEnabled());
        dto.setAccountNonLocked(enseignant.getAccountNonLocked());
        dto.setPremiereConnexion(enseignant.getPremiereConnexion());
        dto.setCreatedAt(enseignant.getCreatedAt());

        Set<String> roleNames = enseignant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        dto.setSpecialite(enseignant.getSpecialite());
        dto.setGrade(enseignant.getGrade());

        if (enseignant.getCoursEnseignes() != null) {
            List<EnseignantDTO.CoursSimpleDTO> cours = enseignant.getCoursEnseignes().stream()
                    .map(c -> {
                        EnseignantDTO.CoursSimpleDTO coursDTO = new EnseignantDTO.CoursSimpleDTO();
                        coursDTO.setId(c.getId());
                        coursDTO.setNomCours(c.getIntitule());
                        coursDTO.setCode(c.getCodeUE());
                        return coursDTO;
                    })
                    .collect(Collectors.toList());
            dto.setCoursEnseignes(cours);
        }

        if (enseignant.getSeances() != null) {
            dto.setNombreSeances(enseignant.getSeances().size());
        }

        return dto;
    }
}

