package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.ClasseDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.EtudiantDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Etudiant;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EtudiantMapper {

    public EtudiantDTO toDTO(Etudiant etudiant) {
        if (etudiant == null) return null;
        EtudiantDTO dto = new EtudiantDTO();

        dto.setId(etudiant.getId());
        dto.setNom(etudiant.getNom());
        dto.setPrenom(etudiant.getPrenom());
        dto.setEmail(etudiant.getEmail());
        dto.setStatut(etudiant.getStatut());
        dto.setEnabled(etudiant.getEnabled());
        dto.setAccountNonLocked(etudiant.getAccountNonLocked());
        dto.setPremiereConnexion(etudiant.getPremiereConnexion());
        dto.setCreatedAt(etudiant.getCreatedAt());

        Set<String> roleNames = etudiant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        dto.setMatricule(etudiant.getMatricule());
        dto.setDateNaissance(etudiant.getDateNaissance());
        dto.setTelephone(etudiant.getTelephone());

        if (etudiant.getClasse() != null) {
            ClasseDTO classeDTO = new ClasseDTO();
            classeDTO.setId(etudiant.getClasse().getId());
            classeDTO.setNomClasse(etudiant.getClasse().getNomClasse());
            classeDTO.setNiveau(etudiant.getClasse().getNiveau());
            dto.setClasse(classeDTO);
        }

        if (etudiant.getPresences() != null) {
            dto.setNombrePresences(etudiant.getPresences().size());
        }

        return dto;
    }
}

