package com.ISI.Appel_classe.service.impl;

import com.ISI.Appel_classe.dto.request.CreateRoleDTO;
import com.ISI.Appel_classe.dto.request.UpdateRolePermissionsDTO;
import com.ISI.Appel_classe.dto.response.RoleDTO;
import com.ISI.Appel_classe.entity.Permission;
import com.ISI.Appel_classe.entity.Role;
import com.ISI.Appel_classe.exception.BadRequestException;
import com.ISI.Appel_classe.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.repository.PermissionRepository;
import com.ISI.Appel_classe.repository.RoleRepository;
import com.ISI.Appel_classe.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec l'ID: " + id));
        return convertToDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO createRole(CreateRoleDTO dto) {
        // Vérifier que le rôle n'existe pas déjà
        if (roleRepository.findByName(dto.getName()).isPresent()) {
            throw new BadRequestException("Un rôle avec ce nom existe déjà");
        }

        Role role = new Role();
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());

        // Associer les permissions
        if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByNameIn(dto.getPermissions());
            if (permissions.size() != dto.getPermissions().size()) {
                throw new BadRequestException("Certaines permissions n'existent pas");
            }
            role.setPermissions(permissions);
        }

        Role saved = roleRepository.save(role);
        log.info("Rôle créé: {}", saved.getName());
        return convertToDTO(saved);
    }

    @Override
    @Transactional
    public RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsDTO dto) {
        // 1. Vérifier que le rôle existe
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé avec l'ID: " + roleId));

        // 2. Vérifier que toutes les permissions existent
        Set<Permission> newPermissions = new HashSet<>();
        if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
            newPermissions = permissionRepository.findByNameIn(dto.getPermissions());
            if (newPermissions.size() != dto.getPermissions().size()) {
                throw new BadRequestException("Certaines permissions n'existent pas");
            }
        }

        // 3. Supprimer les anciennes associations et ajouter les nouvelles
        role.getPermissions().clear();
        role.setPermissions(newPermissions);

        // 4. Sauvegarder
        Role updated = roleRepository.save(role);
        log.info("Permissions du rôle {} mises à jour: {}", role.getName(), dto.getPermissions());

        return convertToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rôle non trouvé avec l'ID: " + id);
        }
        roleRepository.deleteById(id);
        log.info("Rôle supprimé: {}", id);
    }

    @Override
    @Transactional
    public void initializeDefaultRoles() {
        log.info("Initialisation des rôles par défaut...");

        createRoleIfNotExists("ADMIN", "Administrateur système", Set.of(
                "USER_VIEW", "USER_MANAGE", "COURSE_VIEW", "COURSE_MANAGE",
                "ATTENDANCE_VIEW", "ATTENDANCE_MANAGE", "GRADE_VIEW",
                "JUSTIFICATION_VALIDATE", "STATISTICS_VIEW", "STUDENT_VIEW"));

        createRoleIfNotExists("ENSEIGNANT", "Enseignant", Set.of(
                "COURSE_VIEW", "COURSE_MANAGE", "ATTENDANCE_VIEW", "ATTENDANCE_MANAGE",
                "GRADE_VIEW", "STUDENT_VIEW"));

        createRoleIfNotExists("ETUDIANT", "Étudiant", Set.of(
                "COURSE_VIEW", "ATTENDANCE_VIEW", "GRADE_VIEW", "ASSIGNMENT_SUBMIT"));

        createRoleIfNotExists("ASSISTANT_PEDAGOGIQUE", "Assistant pédagogique", Set.of(
                "COURSE_VIEW", "STUDENT_VIEW", "ATTENDANCE_VIEW", "JUSTIFICATION_VALIDATE"));

        createRoleIfNotExists("SURVEILLANT", "Surveillant", Set.of(
                "ATTENDANCE_VIEW", "ATTENDANCE_MANAGE", "STUDENT_VIEW"));

        log.info("Rôles initialisés avec succès");
    }

    private void createRoleIfNotExists(String name, String description, Set<String> permissionNames) {
        if (roleRepository.findByName(name).isEmpty()) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);

            Set<Permission> permissions = permissionRepository.findByNameIn(permissionNames);
            role.setPermissions(permissions);

            roleRepository.save(role);
            log.debug("Rôle créé: {} avec {} permissions", name, permissions.size());
        }
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}
