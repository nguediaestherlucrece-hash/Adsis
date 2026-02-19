package com.Gestion_des_utilisateurs.controller.api;

import com.Gestion_des_utilisateurs.dto.request.CreateRoleDTO;
import com.Gestion_des_utilisateurs.dto.request.UpdateRolePermissionsDTO;
import com.Gestion_des_utilisateurs.dto.response.RoleDTO;
import com.Gestion_des_utilisateurs.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Lister tous les rôles avec leurs permissions
     */
    @GetMapping
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.info("Récupération de tous les rôles");
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    /**
     * Récupérer un rôle par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        log.info("Récupération du rôle avec id={}", id);
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    /**
     * Créer un nouveau rôle
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody CreateRoleDTO dto) {
        log.info("Création d'un nouveau rôle: {}", dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(dto));
    }

    /**
     * Mettre à jour les permissions d'un rôle
     */
    @PutMapping("/{roleId}/permissions")
    public ResponseEntity<RoleDTO> updateRolePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsDTO dto) {
        log.info("Mise à jour des permissions du rôle id={}", roleId);
        return ResponseEntity.ok(roleService.updateRolePermissions(roleId, dto));
    }

    /**
     * Supprimer un rôle
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        log.info("Suppression du rôle id={}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
