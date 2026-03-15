package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.BadRequestException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Permission;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.UserPermissionOverride;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Utilisateur;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utilitaire partagé pour gérer les permissions retirées (overrides) d'un utilisateur.
 * Utilisé par UtilisateurServiceImpl, EnseignantServiceImpl, SurveillantServiceImpl, etc.
 */
@Component
@RequiredArgsConstructor
public class PermissionOverrideHelper {

    private final PermissionRepository permissionRepository;

    /**
     * Applique les permissions retirées à un utilisateur.
     * Valide que chaque permission retirée appartient bien aux rôles de l'utilisateur.
     */
    public void applyRemovedPermissions(Utilisateur utilisateur, Set<String> removedPermissionNames) {
        if (removedPermissionNames == null || removedPermissionNames.isEmpty()) {
            return;
        }

        // Collecter les permissions valides du rôle
        Set<String> rolePermissionNames = utilisateur.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        for (String permName : removedPermissionNames) {
            // Vérifier que cette permission fait partie du rôle
            if (!rolePermissionNames.contains(permName)) {
                throw new BadRequestException(
                        "La permission '" + permName + "' ne fait pas partie des permissions du rôle de l'utilisateur");
            }

            Permission permission = permissionRepository.findByName(permName)
                    .orElseThrow(() -> new BadRequestException("Permission non trouvée: " + permName));

            UserPermissionOverride override = new UserPermissionOverride();
            override.setUtilisateur(utilisateur);
            override.setPermission(permission);
            utilisateur.getPermissionOverrides().add(override);
        }
    }

    /**
     * Met à jour les permissions retirées d'un utilisateur existant.
     * Supprime les anciens overrides et applique les nouveaux.
     */
    public void updateRemovedPermissions(Utilisateur utilisateur, Set<String> removedPermissionNames) {
        if (removedPermissionNames == null) {
            return;
        }
        utilisateur.getPermissionOverrides().clear();
        applyRemovedPermissions(utilisateur, removedPermissionNames);
    }
}
