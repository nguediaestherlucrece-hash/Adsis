package com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AssistantPedagogiqueDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.AssistantPedagogique;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Role;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AssistantPedagogiqueMapper {

    public AssistantPedagogiqueDTO toDTO(AssistantPedagogique assistant) {
        if (assistant == null) return null;
        AssistantPedagogiqueDTO dto = new AssistantPedagogiqueDTO();

        dto.setId(assistant.getId());
        dto.setNom(assistant.getNom());
        dto.setPrenom(assistant.getPrenom());
        dto.setEmail(assistant.getEmail());
        dto.setStatut(assistant.getStatut());
        dto.setEnabled(assistant.getEnabled());
        dto.setAccountNonLocked(assistant.getAccountNonLocked());
        dto.setPremiereConnexion(assistant.getPremiereConnexion());
        dto.setCreatedAt(assistant.getCreatedAt());
        dto.setFiliereId(assistant.getFiliere() != null ? assistant.getFiliere().getId() : null);

        Set<String> roleNames = assistant.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoleNames(roleNames);

        // Permissions
        Set<String> rolePermissions = assistant.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        dto.setRolePermissions(rolePermissions);

        Set<String> removedPermissions = new java.util.HashSet<>();
        if (assistant.getPermissionOverrides() != null) {
            removedPermissions = assistant.getPermissionOverrides().stream()
                    .map(o -> o.getPermission().getName())
                    .collect(Collectors.toSet());
        }
        dto.setRemovedPermissions(removedPermissions);

        Set<String> effectivePermissions = new java.util.HashSet<>(rolePermissions);
        effectivePermissions.removeAll(removedPermissions);
        dto.setEffectivePermissions(effectivePermissions);

        if (assistant.getJustificatifsValides() != null) {
            dto.setNombreJustificatifsValides(assistant.getJustificatifsValides().size());
        }

        if (assistant.getEmploisDuTemps() != null) {
            dto.setNombreEmploisDuTemps(assistant.getEmploisDuTemps().size());
        }

        return dto;
    }
}
