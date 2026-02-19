package com.Gestion_des_utilisateurs.controller.api;

import com.Gestion_des_utilisateurs.dto.request.EnseignantCreateDTO;
import com.Gestion_des_utilisateurs.dto.request.EnseignantUpdateDTO;
import com.Gestion_des_utilisateurs.dto.response.EnseignantDTO;
import com.Gestion_des_utilisateurs.service.EnseignantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST pour la gestion des enseignants
 */
@Slf4j
@RestController
@RequestMapping("/api/enseignants")
@RequiredArgsConstructor
public class EnseignantController {

    private final EnseignantService enseignantService;

    @GetMapping
    @PreAuthorize("hasAuthority('ENSEIGNANT_READ')")
    public ResponseEntity<List<EnseignantDTO>> getAllEnseignants() {
        log.info("📖 [GET /api/enseignants] Récupération de tous les enseignants");
        return ResponseEntity.ok(enseignantService.getAllEnseignants());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_READ')")
    public ResponseEntity<EnseignantDTO> getById(@PathVariable Long id) {
        log.info("📖 [GET /api/enseignants/{}]", id);
        return ResponseEntity.ok(enseignantService.getEnseignantById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_READ')")
    public ResponseEntity<EnseignantDTO> getByEmail(@PathVariable String email) {
        log.info("📖 [GET /api/enseignants/email/{}]", email);
        return ResponseEntity.ok(enseignantService.getEnseignantByEmail(email));
    }

    @GetMapping("/specialite/{specialite}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_READ')")
    public ResponseEntity<List<EnseignantDTO>> getBySpecialite(@PathVariable String specialite) {
        log.info("📖 [GET /api/enseignants/specialite/{}]", specialite);
        return ResponseEntity.ok(enseignantService.getEnseignantsBySpecialite(specialite));
    }

    @GetMapping("/grade/{grade}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_READ')")
    public ResponseEntity<List<EnseignantDTO>> getByGrade(@PathVariable String grade) {
        log.info("📖 [GET /api/enseignants/grade/{}]", grade);
        return ResponseEntity.ok(enseignantService.getEnseignantsByGrade(grade));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENSEIGNANT_CREATE')")
    public ResponseEntity<EnseignantDTO> create(@RequestBody EnseignantCreateDTO dto) {
        log.info("➕ [POST /api/enseignants] Création: {}", dto.getEmail());
        EnseignantDTO created = enseignantService.createEnseignant(dto);
        log.info("✅ Enseignant créé - ID: {}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_UPDATE')")
    public ResponseEntity<EnseignantDTO> update(
            @PathVariable Long id,
            @RequestBody EnseignantUpdateDTO dto) {
        log.info("✏️ [PUT /api/enseignants/{}]", id);
        return ResponseEntity.ok(enseignantService.updateEnseignant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ENSEIGNANT_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("🗑️ [DELETE /api/enseignants/{}]", id);
        enseignantService.deleteEnseignant(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cours")
    @PreAuthorize("hasAuthority('ENSEIGNANT_UPDATE')")
    public ResponseEntity<EnseignantDTO> assignerCours(
            @PathVariable Long id,
            @RequestBody List<Long> coursIds) {
        log.info("🔄 [PATCH /api/enseignants/{}/cours] Assignation de {} cours", id, coursIds.size());
        return ResponseEntity.ok(enseignantService.assignerCours(id, coursIds));
    }
}
