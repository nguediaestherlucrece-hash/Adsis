package com.ISI.Appel_classe.Gestion_des_utilisateurs.service.impl;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.PermissionDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.model.Permission;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.exception.ResourceNotFoundException;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.repository.PermissionRepository;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.PermissionService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.mappers.PermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper mapper;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionDTO getPermissionById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée avec l'ID: " + id));
        return mapper.toDTO(permission);
    }

    @Override
    @Transactional
    public void initializeDefaultPermissions() {
        log.info("Initialisation des permissions par défaut...");

        createPermissionIfNotExists("COURSE_VIEW", "Voir les cours");
        createPermissionIfNotExists("COURSE_MANAGE", "Gérer les cours");
        createPermissionIfNotExists("USER_VIEW", "Voir les utilisateurs");
        createPermissionIfNotExists("USER_MANAGE", "Gérer les utilisateurs");
        createPermissionIfNotExists("STUDENT_VIEW", "Voir les étudiants");
        createPermissionIfNotExists("ATTENDANCE_VIEW", "Voir les présences");
        createPermissionIfNotExists("ATTENDANCE_MANAGE", "Gérer les présences");
        createPermissionIfNotExists("GRADE_VIEW", "Voir les notes");
        createPermissionIfNotExists("ASSIGNMENT_SUBMIT", "Soumettre les devoirs");
        createPermissionIfNotExists("JUSTIFICATION_VALIDATE", "Valider les justificatifs");
        createPermissionIfNotExists("STATISTICS_VIEW", "Voir les statistiques");

        log.info("Permissions initialisées avec succès");
    }

    private void createPermissionIfNotExists(String name, String description) {
        if (permissionRepository.findByName(name).isEmpty()) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.debug("Permission créée: {}", name);
        }
    }


}
