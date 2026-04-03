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
        dto.setFiliereId(surveillant.getFiliere() != null ? surveillant.getFiliere().getId() : null);

        Set<String> roleNames = surveillant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // Permissions
        Set<String> rolePermissions = surveillant.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        dto.setRolePermissions(rolePermissions);

        Set<String> removedPermissions = new java.util.HashSet<>();
        if (surveillant.getPermissionOverrides() != null) {
            removedPermissions = surveillant.getPermissionOverrides().stream()
                    .map(o -> o.getPermission().getName())
                    .collect(Collectors.toSet());
        }
        dto.setRemovedPermissions(removedPermissions);

        Set<String> effectivePermissions = new java.util.HashSet<>(rolePermissions);
        effectivePermissions.removeAll(removedPermissions);
        dto.setEffectivePermissions(effectivePermissions);

        if (surveillant.getSeances() != null) {
            dto.setNombreSeances(surveillant.getSeances().size());
        }

        return dto;
    }
}
