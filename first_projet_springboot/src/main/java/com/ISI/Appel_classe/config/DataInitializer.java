package com.ISI.Appel_classe.config;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.PermissionService;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PermissionService permissionService;
    private final RoleService roleService;

    @Override
    public void run(String... args) {
        log.info("=== Initialisation des données de base ===");

        // 1. Initialiser les permissions
        permissionService.initializeDefaultPermissions();

        // 2. Initialiser les rôles avec leurs permissions
        roleService.initializeDefaultRoles();

        log.info("=== Initialisation terminée ===");
    }
}
