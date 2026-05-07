package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.UtilisateurDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Permission;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setMatricule(utilisateur.getMatricule());
        dto.setStatut(utilisateur.getStatut());
        dto.setPremiereConnexion(utilisateur.getPremiereConnexion());
        dto.setCreatedAt(utilisateur.getCreatedAt());

        Set<String> roleNames = utilisateur.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // Permissions du rôle
        Set<String> rolePermissions = utilisateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
        dto.setRolePermissions(rolePermissions);

        // Permissions retirées
        Set<String> removedPermissions = new HashSet<>();
        if (utilisateur.getPermissionOverrides() != null) {
            removedPermissions = utilisateur.getPermissionOverrides().stream()
                    .map(o -> o.getPermission().getName())
                    .collect(Collectors.toSet());
        }
        dto.setRemovedPermissions(removedPermissions);

        // Permissions effectives = rôle - retirées
        Set<String> effectivePermissions = new HashSet<>(rolePermissions);
        effectivePermissions.removeAll(removedPermissions);
        dto.setEffectivePermissions(effectivePermissions);

        return dto;
    }
}
