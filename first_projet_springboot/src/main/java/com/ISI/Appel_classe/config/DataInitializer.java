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

    private final PermissionService     permissionService;
    private final RoleService           roleService;
    private final SeedUsersInitializer  seedUsersInitializer;

    @Override
    public void run(String... args) {
        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║       ADSIS — Initialisation au démarrage        ║");
        log.info("╚══════════════════════════════════════════════════╝");

        // Étape 1 — Permissions (doivent exister avant les rôles)
        log.info("── Étape 1 : Permissions");
        permissionService.initializeDefaultPermissions();

        // Étape 2 — Rôles (associés aux permissions)
        log.info("── Étape 2 : Rôles");
        roleService.initializeDefaultRoles();

        // Étape 3 — Comptes maîtres (clés de secours par rôle)
        log.info("── Étape 3 : Comptes maîtres");
        seedUsersInitializer.seedDefaultUsers();

        log.info("╔══════════════════════════════════════════════════╗");
        log.info("║              Initialisation terminée             ║");
        log.info("╚══════════════════════════════════════════════════╝");
    }
}

