package com.ISI.Appel_classe.controller.api;

import com.ISI.Appel_classe.dto.response.PermissionDTO;
import com.ISI.Appel_classe.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Lister toutes les permissions disponibles
     */
    @GetMapping
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        log.info("Récupération de toutes les permissions");
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /**
     * Récupérer une permission par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable Long id) {
        log.info("Récupération de la permission avec id={}", id);
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }
}
