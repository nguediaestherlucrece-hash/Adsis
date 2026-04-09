package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AdministrateurDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Administrateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdministrateurMapper {

    public AdministrateurDTO toDTO(Administrateur administrateur) {
        if (administrateur == null) return null;
        AdministrateurDTO dto = new AdministrateurDTO();

        dto.setId(administrateur.getId());
        dto.setNom(administrateur.getNom());
        dto.setPrenom(administrateur.getPrenom());
        dto.setEmail(administrateur.getEmail());
        dto.setMatricule(administrateur.getMatricule());
        dto.setStatut(administrateur.getStatut());
        dto.setEnabled(administrateur.getEnabled());
        dto.setAccountNonLocked(administrateur.getAccountNonLocked());
        dto.setPremiereConnexion(administrateur.getPremiereConnexion());
        dto.setCreatedAt(administrateur.getCreatedAt());

        Set<String> roleNames = administrateur.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // Permissions
        Set<String> rolePermissions = administrateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        dto.setRolePermissions(rolePermissions);

        Set<String> removedPermissions = new java.util.HashSet<>();
        if (administrateur.getPermissionOverrides() != null) {
            removedPermissions = administrateur.getPermissionOverrides().stream()
                    .map(o -> o.getPermission().getName())
                    .collect(Collectors.toSet());
        }
        dto.setRemovedPermissions(removedPermissions);

        Set<String> effectivePermissions = new java.util.HashSet<>(rolePermissions);
        effectivePermissions.removeAll(removedPermissions);
        dto.setEffectivePermissions(effectivePermissions);

        return dto;
    }
}
