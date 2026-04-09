package com.ISI.Appel_classe.Gestion_des_utilisateurs.controller.api;

import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurCreateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.request.AdministrateurUpdateDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.dto.response.AdministrateurDTO;
import com.ISI.Appel_classe.Gestion_des_utilisateurs.service.serviceInterface.AdministrateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour la gestion des administrateurs
 */
@Slf4j
@RestController
@RequestMapping("/api/administrateurs")
@RequiredArgsConstructor
public class AdministrateurController {

    private final AdministrateurService administrateurService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdministrateurDTO>> getAllAdministrateurs() {
        log.info("📖 [GET /api/administrateurs] Récupération de tous les administrateurs");
        return ResponseEntity.ok(administrateurService.getAllAdministrateurs());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrateurDTO> getById(@PathVariable Long id) {
        log.info("📖 [GET /api/administrateurs/{}]", id);
        return ResponseEntity.ok(administrateurService.getAdministrateurById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrateurDTO> getByEmail(@PathVariable String email) {
        log.info("📖 [GET /api/administrateurs/email/{}]", email);
        return ResponseEntity.ok(administrateurService.getAdministrateurByEmail(email));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrateurDTO> create(@RequestBody AdministrateurCreateDTO dto) {
        log.info("➕ [POST /api/administrateurs] Création: {}", dto.getEmail());
        AdministrateurDTO created = administrateurService.createAdministrateur(dto);
        log.info("✅ Administrateur créé - ID: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdministrateurDTO> update(
            @PathVariable Long id,
            @RequestBody AdministrateurUpdateDTO dto) {
        log.info("✏️ [PUT /api/administrateurs/{}]", id);
        return ResponseEntity.ok(administrateurService.updateAdministrateur(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("🗑️ [DELETE /api/administrateurs/{}]", id);
        administrateurService.deleteAdministrateur(id);
        return ResponseEntity.noContent().build();
    }
}
